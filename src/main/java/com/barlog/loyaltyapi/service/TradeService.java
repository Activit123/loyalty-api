package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.*;
import com.barlog.loyaltyapi.exception.ResourceNotFoundException;
import com.barlog.loyaltyapi.model.*;
import com.barlog.loyaltyapi.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepository tradeRepository;
    private final TradeOfferItemRepository offerItemRepository;
    private final UserInventoryItemRepository inventoryItemRepository; // Produse Fizice
    private final UserItemRepository userItemRepository;               // Iteme RPG
    private final UserRepository userRepository;
    private final CoinTransactionRepository coinTransactionRepository;
    private final AdminService adminService;
    private final InventoryService inventoryService;
    private final ItemService itemService; // Pentru mapare

    private final EntityManager entityManager;
    private final UserNotificationService notificationService;

    // --- Helpers ---
    private User findUserByIdentifier(String identifier) {
        return userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByNickname(identifier).map(u -> (User)u))
                .orElseThrow(() -> new ResourceNotFoundException("Utilizatorul '" + identifier + "' nu a fost găsit."));
    }

    private Trade findActiveTrade(Long tradeId) {
        Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Tranzacția nu a fost găsită."));
        if (trade.getStatus() == TradeStatus.COMPLETED || trade.getStatus() == TradeStatus.CANCELED) {
            throw new IllegalStateException("Tranzacția nu este activă.");
        }
        return trade;
    }

    // --- 1. VALIDARE ---
    private void validateOffer(User user, Integer coins, List<Long> invItemIds, List<Long> userItemIds, Long currentTradeId) {
        // Monede
        if (coins != null && coins > 0 && user.getCoins() < coins) {
            throw new IllegalStateException("Fonduri insuficiente!");
        }

        // Produse Fizice
        if (invItemIds != null && !invItemIds.isEmpty()) {
            List<UserInventoryItem> items = inventoryItemRepository.findAllById(invItemIds);
            if (items.size() != invItemIds.size()) throw new ResourceNotFoundException("Produse fizice lipsă.");
            boolean allOwned = items.stream().allMatch(item -> item.getUser().getId().equals(user.getId()) && "IN_INVENTORY".equals(item.getStatus()));
            if (!allOwned) throw new IllegalStateException("Nu deții toate produsele fizice selectate.");
        }

        // --- ITEME RPG (NOU) ---
        if (userItemIds != null && !userItemIds.isEmpty()) {
            List<UserItem> rpgItems = userItemRepository.findAllById(userItemIds);

            if (rpgItems.size() != userItemIds.size()) {
                throw new ResourceNotFoundException("Unul sau mai multe iteme de echipament nu au fost găsite.");
            }

            // Verificăm proprietarul
            boolean allOwned = rpgItems.stream().allMatch(item -> item.getUser().getId().equals(user.getId()));
            if (!allOwned) throw new IllegalStateException("Nu deții toate itemele de echipament selectate.");

            // Verificăm dacă sunt echipate (NU POȚI DA TRADE LA CE PORȚI)
            if (rpgItems.stream().anyMatch(UserItem::isEquipped)) {
                throw new IllegalStateException("Nu poți tranzacționa iteme echipate! Dă-le jos întâi.");
            }
        }
    }

    // --- 2. SALVARE OFERTĂ ---
    private void saveOfferItems(Trade trade, User user, Integer coins, List<Long> invItemIds, List<Long> userItemIds) {
        offerItemRepository.deleteByTradeAndUser(trade, user);

        // Monede
        if (coins != null && coins > 0) {
            offerItemRepository.save(TradeOfferItem.builder()
                    .trade(trade).user(user).itemType(TradeOfferItemType.COINS).offeredAmount(coins).build());
        }

        // Produse Fizice
        if (invItemIds != null && !invItemIds.isEmpty()) {
            List<UserInventoryItem> items = inventoryItemRepository.findAllById(invItemIds);
            items.forEach(item -> offerItemRepository.save(TradeOfferItem.builder()
                    .trade(trade).user(user).itemType(TradeOfferItemType.INVENTORY_ITEM).inventoryItem(item).build()));
        }

        // --- ITEME RPG (NOU) ---
        if (userItemIds != null && !userItemIds.isEmpty()) {
            List<UserItem> items = userItemRepository.findAllById(userItemIds);
            items.forEach(item -> offerItemRepository.save(TradeOfferItem.builder()
                    .trade(trade).user(user).itemType(TradeOfferItemType.EQUIPMENT_ITEM).userItem(item).build()));
        }
    }

    // --- 3. MAPARE CĂTRE FRONTEND ---
    private TradeOfferItemDto mapOfferItemToDto(TradeOfferItem item) {
        TradeOfferItemDto dto = new TradeOfferItemDto();
        dto.setId(item.getId());
        dto.setItemType(item.getItemType());
        dto.setOfferedAmount(item.getOfferedAmount());

        if (item.getItemType() == TradeOfferItemType.INVENTORY_ITEM && item.getInventoryItem() != null) {
            dto.setInventoryItem(inventoryService.mapToDto(item.getInventoryItem()));
        }

        // --- MAPARE RPG (NOU) ---
        if (item.getItemType() == TradeOfferItemType.EQUIPMENT_ITEM && item.getUserItem() != null) {
            dto.setUserItem(itemService.mapToDto(item.getUserItem()));
        }

        return dto;
    }

    // --- 4. TRANSFER ---
    private void transferItems(List<TradeOfferItemDto> senderOffer, User receiver) {
        for (TradeOfferItemDto offerItem : senderOffer) {

            // Produs Fizic
            if (offerItem.getItemType() == TradeOfferItemType.INVENTORY_ITEM) {
                UserInventoryItem item = inventoryItemRepository.findById(offerItem.getInventoryItem().getId()).orElseThrow();
                item.setUser(receiver);
                inventoryItemRepository.save(item);
            }

            // --- ITEM RPG (NOU) ---
            else if (offerItem.getItemType() == TradeOfferItemType.EQUIPMENT_ITEM) {
                Long userItemId = offerItem.getUserItem().getId(); // Luăm ID-ul din DTO
                UserItem item = userItemRepository.findById(userItemId).orElseThrow();

                item.setUser(receiver);
                item.setEquipped(false); // Siguranță
                userItemRepository.save(item);
            }
        }
    }

    private void processUserTrade(User sender, List<TradeOfferItemDto> senderOffer, User receiver) {
        Integer coinsOffered = senderOffer.stream()
                .filter(item -> item.getItemType() == TradeOfferItemType.COINS)
                .map(TradeOfferItemDto::getOfferedAmount)
                .findFirst().orElse(0);

        if (coinsOffered > 0) {
            sender.setCoins(sender.getCoins() - coinsOffered);
            receiver.setCoins(receiver.getCoins() + coinsOffered);
            userRepository.save(sender);
            userRepository.save(receiver);

            // Log tranzacții
            coinTransactionRepository.save(CoinTransaction.builder().user(sender).amount(-coinsOffered).description("Trade OUT").transactionType("TRADE_OUT").createdAt(LocalDateTime.now()).build());
            coinTransactionRepository.save(CoinTransaction.builder().user(receiver).amount(coinsOffered).description("Trade IN").transactionType("TRADE_IN").createdAt(LocalDateTime.now()).build());
        }
    }

    // --- ENDPOINTS PRINCIPALE ---

    @Transactional
    public TradeDetailsDto initiateTrade(User initiator, TradeInitiateRequest request) {
        User recipient = findUserByIdentifier(request.getRecipientIdentifier());
        if (initiator.getId().equals(recipient.getId())) throw new IllegalStateException("Self-trade invalid.");

        validateOffer(initiator, request.getOfferedCoins(), request.getOfferedInventoryItemIds(), request.getOfferedUserItemIds(), 0L);

        Trade trade = tradeRepository.save(Trade.builder()
                .initiator(initiator).recipient(recipient).status(TradeStatus.INITIATED)
                .initiatorAgreed(true).recipientAgreed(false).build());

        saveOfferItems(trade, initiator, request.getOfferedCoins(), request.getOfferedInventoryItemIds(), request.getOfferedUserItemIds());

        notificationService.notifyUser(recipient, initiator.getNickname() + " vrea trade!", NotificationType.TRADE_OFFER, "/trade");
        return getTradeDetails(trade.getId());
    }

    @Transactional
    public TradeDetailsDto makeOffer(User user, Long tradeId, TradeOfferRequest request) {
        Trade trade = findActiveTrade(tradeId);

        validateOffer(user, request.getOfferedCoins(), request.getOfferedInventoryItemIds(), request.getOfferedUserItemIds(), tradeId);
        saveOfferItems(trade, user, request.getOfferedCoins(), request.getOfferedInventoryItemIds(), request.getOfferedUserItemIds());

        boolean isInitiator = trade.getInitiator().getId().equals(user.getId());
        User partner = isInitiator ? trade.getRecipient() : trade.getInitiator();

        notificationService.notifyUser(partner, "Oferta actualizată de " + user.getNickname(), NotificationType.TRADE_UPDATE, "/trade");

        if (isInitiator) {
            trade.setInitiatorAgreed(request.isAcceptsFinalOffer());
            if (!request.isAcceptsFinalOffer()) trade.setRecipientAgreed(false);
        } else {
            trade.setRecipientAgreed(request.isAcceptsFinalOffer());
            if (!request.isAcceptsFinalOffer()) trade.setInitiatorAgreed(false);
        }

        if (trade.isInitiatorAgreed() && trade.isRecipientAgreed()) trade.setStatus(TradeStatus.ACCEPTED);
        else trade.setStatus(TradeStatus.PENDING_APPROVAL);

        return getTradeDetails(tradeRepository.save(trade).getId());
    }

    @Transactional
    public TradeDetailsDto completeTrade(User user, Long tradeId) {
        Trade trade = entityManager.find(Trade.class, tradeId, LockModeType.PESSIMISTIC_WRITE);
        if (trade.getStatus() != TradeStatus.ACCEPTED) throw new IllegalStateException("Trade not accepted.");

        TradeDetailsDto details = getTradeDetails(tradeId);
        User init = userRepository.findById(details.getInitiator().getId()).orElseThrow();
        User recv = userRepository.findById(details.getRecipient().getId()).orElseThrow();

        processUserTrade(init, details.getInitiatorOffer(), recv);
        processUserTrade(recv, details.getRecipientOffer(), init);

        transferItems(details.getInitiatorOffer(), recv);
        transferItems(details.getRecipientOffer(), init);

        trade.setStatus(TradeStatus.COMPLETED);
        tradeRepository.save(trade);

        notificationService.notifyUser(init, "Trade Reușit!", NotificationType.TRADE_UPDATE, "/inventory");
        notificationService.notifyUser(recv, "Trade Reușit!", NotificationType.TRADE_UPDATE, "/inventory");

        return getTradeDetails(tradeId);
    }

    @Transactional
    public void cancelTrade(User user, Long tradeId) {
        Trade trade = findActiveTrade(tradeId);
        if (!trade.getInitiator().getId().equals(user.getId()) && !trade.getRecipient().getId().equals(user.getId())) throw new IllegalStateException("Not your trade.");

        trade.setStatus(TradeStatus.CANCELED);
        offerItemRepository.deleteByTradeAndUser(trade, trade.getInitiator());
        offerItemRepository.deleteByTradeAndUser(trade, trade.getRecipient());
        tradeRepository.save(trade);
    }

    @Transactional
    public TradeDetailsDto getTradeDetails(Long tradeId) {
        Trade trade = tradeRepository.findById(tradeId).orElseThrow(() -> new ResourceNotFoundException("Trade not found"));
        List<TradeOfferItem> offers = offerItemRepository.findByTrade(trade);

        TradeDetailsDto dto = new TradeDetailsDto();
        dto.setTradeId(trade.getId());
        dto.setStatus(trade.getStatus());
        dto.setInitiator(adminService.mapUserToDto(trade.getInitiator()));
        dto.setRecipient(adminService.mapUserToDto(trade.getRecipient()));
        dto.setInitiatorAgreed(trade.isInitiatorAgreed());
        dto.setRecipientAgreed(trade.isRecipientAgreed());

        dto.setInitiatorOffer(offers.stream().filter(o -> o.getUser().getId().equals(trade.getInitiator().getId())).map(this::mapOfferItemToDto).collect(Collectors.toList()));
        dto.setRecipientOffer(offers.stream().filter(o -> o.getUser().getId().equals(trade.getRecipient().getId())).map(this::mapOfferItemToDto).collect(Collectors.toList()));

        return dto;
    }

    public List<TradeDetailsDto> getActiveTrades(User user) {
        return tradeRepository.findActiveTradesByUser(user).stream().map(t -> getTradeDetails(t.getId())).collect(Collectors.toList());
    }
}