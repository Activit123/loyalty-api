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

    // --- Dependențe ---
    private final TradeRepository tradeRepository;
    private final TradeOfferItemRepository offerItemRepository;
    private final UserInventoryItemRepository inventoryItemRepository; // Doar produse fizice
    private final UserRepository userRepository;
    private final CoinTransactionRepository coinTransactionRepository;
    private final AdminService adminService;
    private final InventoryService inventoryService;
    private final EntityManager entityManager;
    private final UserNotificationService notificationService;

    // AM SCOS: UserItemRepository și ItemService (nu mai sunt necesare fără iteme RPG)

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

    // --- 1. VALIDARE (Doar Monede + Produse Fizice) ---
    // Am scos parametrul userItemIds
    private void validateOffer(User user, Integer coins, List<Long> invItemIds, Long currentTradeId) {

        // A. Validare monede
        if (coins != null && coins > 0 && user.getCoins() < coins) {
            throw new IllegalStateException("Fonduri insuficiente! Utilizatorul " + user.getEmail() + " nu are " + coins + " monede.");
        }

        // B. Validare produse fizice (InventoryItem)
        if (invItemIds != null && !invItemIds.isEmpty()) {
            List<UserInventoryItem> items = inventoryItemRepository.findAllById(invItemIds);

            // Verificăm dacă toate ID-urile au fost găsite
            if (items.size() != invItemIds.size()) {
                throw new ResourceNotFoundException("Unul sau mai multe produse fizice din ofertă nu au fost găsite.");
            }

            // Verificăm ownership și status
            boolean allOwned = items.stream().allMatch(item -> item.getUser().getId().equals(user.getId()) && "IN_INVENTORY".equals(item.getStatus()));
            if (!allOwned) {
                throw new IllegalStateException("Nu deții toate produsele fizice selectate sau au fost deja folosite.");
            }
        }
    }

    // --- 2. SALVARE OFERTĂ (Doar Monede + Produse Fizice) ---
    // Am scos parametrul userItemIds
    private void saveOfferItems(Trade trade, User user, Integer coins, List<Long> invItemIds) {
        // Șterge orice ofertă veche a acestui user pentru acest trade
        offerItemRepository.deleteByTradeAndUser(trade, user);

        // A. Salvează Monede
        if (coins != null && coins > 0) {
            offerItemRepository.save(TradeOfferItem.builder()
                    .trade(trade)
                    .user(user)
                    .itemType(TradeOfferItemType.COINS)
                    .offeredAmount(coins)
                    .build());
        }

        // B. Salvează Produse Fizice
        if (invItemIds != null && !invItemIds.isEmpty()) {
            List<UserInventoryItem> items = inventoryItemRepository.findAllById(invItemIds);
            items.forEach(item -> {
                offerItemRepository.save(TradeOfferItem.builder()
                        .trade(trade)
                        .user(user)
                        .itemType(TradeOfferItemType.INVENTORY_ITEM)
                        .inventoryItem(item)
                        .build());
            });
        }
    }

    // --- 3. MAPPER DTO ---
    private TradeOfferItemDto mapOfferItemToDto(TradeOfferItem item) {
        TradeOfferItemDto dto = new TradeOfferItemDto();
        dto.setId(item.getId());
        dto.setItemType(item.getItemType());
        dto.setOfferedAmount(item.getOfferedAmount());

        // Doar Produs Fizic
        if (item.getItemType() == TradeOfferItemType.INVENTORY_ITEM && item.getInventoryItem() != null) {
            dto.setInventoryItem(inventoryService.mapToDto(item.getInventoryItem()));
        }

        return dto;
    }

    // --- 4. TRANSFER PROPRIETATE ---
    private void transferItems(List<TradeOfferItemDto> senderOffer, User receiver) {
        for (TradeOfferItemDto offerItem : senderOffer) {

            // Doar Produs Fizic (InventoryItem)
            if (offerItem.getItemType() == TradeOfferItemType.INVENTORY_ITEM) {
                UserInventoryItem item = inventoryItemRepository.findById(offerItem.getInventoryItem().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Produs fizic negăsit."));
                item.setUser(receiver);
                inventoryItemRepository.save(item);
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
            coinTransactionRepository.save(CoinTransaction.builder()
                    .user(sender)
                    .amount(-coinsOffered)
                    .description("Trade OUT către " + receiver.getEmail())
                    .transactionType("TRADE_OUT")
                    .createdAt(LocalDateTime.now())
                    .build());

            receiver.setCoins(receiver.getCoins() + coinsOffered);
            coinTransactionRepository.save(CoinTransaction.builder()
                    .user(receiver)
                    .amount(coinsOffered)
                    .description("Trade IN de la " + sender.getEmail())
                    .transactionType("TRADE_IN")
                    .createdAt(LocalDateTime.now())
                    .build());

            userRepository.save(sender);
            userRepository.save(receiver);
        }
    }

    // --- FUNCȚIONALITĂȚI PUBLICE ---

    @Transactional
    public TradeDetailsDto initiateTrade(User initiator, TradeInitiateRequest request) {
        User recipient = findUserByIdentifier(request.getRecipientIdentifier());

        if (initiator.getId().equals(recipient.getId())) {
            throw new IllegalStateException("Nu poți tranzacționa cu tine însuți.");
        }

        // Ignorăm request.getOfferedUserItemIds() aici
        validateOffer(initiator, request.getOfferedCoins(), request.getOfferedInventoryItemIds(), 0L);

        Trade newTrade = Trade.builder()
                .initiator(initiator)
                .recipient(recipient)
                .status(TradeStatus.INITIATED)
                .initiatorAgreed(true)
                .recipientAgreed(false)
                .build();
        Trade savedTrade = tradeRepository.save(newTrade);

        // Salvăm doar monede și produse fizice
        saveOfferItems(savedTrade, initiator, request.getOfferedCoins(), request.getOfferedInventoryItemIds());

        notificationService.notifyUser(
                recipient,
                initiator.getNickname() + " vrea să facă un schimb cu tine!",
                NotificationType.TRADE_OFFER,
                "/trade"
        );
        return getTradeDetails(savedTrade.getId());
    }

    @Transactional
    public TradeDetailsDto makeOffer(User user, Long tradeId, TradeOfferRequest request) {
        Trade trade = findActiveTrade(tradeId);

        if (!trade.getInitiator().getId().equals(user.getId()) && !trade.getRecipient().getId().equals(user.getId())) {
            throw new IllegalStateException("Nu ești parte în această tranzacție.");
        }

        // Ignorăm request.getOfferedUserItemIds()
        validateOffer(user, request.getOfferedCoins(), request.getOfferedInventoryItemIds(), tradeId);
        saveOfferItems(trade, user, request.getOfferedCoins(), request.getOfferedInventoryItemIds());

        boolean isInitiator = trade.getInitiator().getId().equals(user.getId());
        User partner = isInitiator ? trade.getRecipient() : trade.getInitiator();

        String message = request.isAcceptsFinalOffer()
                ? user.getNickname() + " a ACCEPTAT oferta ta de schimb!"
                : user.getNickname() + " a actualizat oferta de schimb.";

        notificationService.notifyUser(partner, message, NotificationType.TRADE_UPDATE, "/trade");

        if (isInitiator) {
            trade.setInitiatorAgreed(request.isAcceptsFinalOffer());
            if (!request.isAcceptsFinalOffer()) trade.setRecipientAgreed(false);
        } else {
            trade.setRecipientAgreed(request.isAcceptsFinalOffer());
            if (!request.isAcceptsFinalOffer()) trade.setInitiatorAgreed(false);
        }

        if (trade.isInitiatorAgreed() && trade.isRecipientAgreed()) {
            trade.setStatus(TradeStatus.ACCEPTED);
        } else {
            trade.setStatus(TradeStatus.PENDING_APPROVAL);
        }

        tradeRepository.save(trade);
        return getTradeDetails(tradeId);
    }

    @Transactional
    public TradeDetailsDto completeTrade(User user, Long tradeId) {
        Trade trade = entityManager.find(Trade.class, tradeId, LockModeType.PESSIMISTIC_WRITE);

        if (trade == null) throw new ResourceNotFoundException("Tranzacția nu a fost găsită.");
        if (trade.getStatus() != TradeStatus.ACCEPTED) {
            throw new IllegalStateException("Tranzacția nu este ACCEPTED și nu poate fi finalizată.");
        }

        TradeDetailsDto details = getTradeDetails(tradeId);

        User initiatorEntity = userRepository.findById(details.getInitiator().getId()).orElseThrow();
        User recipientEntity = userRepository.findById(details.getRecipient().getId()).orElseThrow();

        // Transferăm monedele
        processUserTrade(initiatorEntity, details.getInitiatorOffer(), recipientEntity);
        processUserTrade(recipientEntity, details.getRecipientOffer(), initiatorEntity);

        // Transferăm produsele fizice
        transferItems(details.getInitiatorOffer(), recipientEntity);
        transferItems(details.getRecipientOffer(), initiatorEntity);

        trade.setStatus(TradeStatus.COMPLETED);
        tradeRepository.save(trade);

        notificationService.notifyUser(trade.getInitiator(), "Trade finalizat cu succes!", NotificationType.TRADE_UPDATE, "/inventory");
        notificationService.notifyUser(trade.getRecipient(), "Trade finalizat cu succes!", NotificationType.TRADE_UPDATE, "/inventory");

        return getTradeDetails(tradeId);
    }

    @Transactional
    public void cancelTrade(User user, Long tradeId) {
        Trade trade = findActiveTrade(tradeId);

        if (!trade.getInitiator().getId().equals(user.getId()) && !trade.getRecipient().getId().equals(user.getId())) {
            throw new IllegalStateException("Nu poți anula o tranzacție în care nu ești parte.");
        }

        if (trade.getStatus() == TradeStatus.COMPLETED) {
            throw new IllegalStateException("Tranzacția a fost deja finalizată și nu poate fi anulată.");
        }

        offerItemRepository.deleteByTradeAndUser(trade, trade.getInitiator());
        offerItemRepository.deleteByTradeAndUser(trade, trade.getRecipient());

        trade.setStatus(TradeStatus.CANCELED);
        tradeRepository.save(trade);

        User partner = trade.getInitiator().getId().equals(user.getId()) ? trade.getRecipient() : trade.getInitiator();
        notificationService.notifyUser(partner, user.getNickname() + " a ANULAT schimbul.", NotificationType.TRADE_UPDATE, "/trade");
    }

    @Transactional
    public TradeDetailsDto getTradeDetails(Long tradeId) {
        Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Tranzacția nu a fost găsită."));

        List<TradeOfferItem> allOffers = offerItemRepository.findByTrade(trade);

        List<TradeOfferItemDto> initiatorOffer = allOffers.stream()
                .filter(item -> item.getUser().getId().equals(trade.getInitiator().getId()))
                .map(this::mapOfferItemToDto)
                .collect(Collectors.toList());

        List<TradeOfferItemDto> recipientOffer = allOffers.stream()
                .filter(item -> item.getUser().getId().equals(trade.getRecipient().getId()))
                .map(this::mapOfferItemToDto)
                .collect(Collectors.toList());

        TradeDetailsDto dto = new TradeDetailsDto();
        dto.setTradeId(trade.getId());
        dto.setStatus(trade.getStatus());
        dto.setCreatedAt(trade.getCreatedAt());

        dto.setInitiator(adminService.mapUserToDto(trade.getInitiator()));
        dto.setRecipient(adminService.mapUserToDto(trade.getRecipient()));

        dto.setInitiatorOffer(initiatorOffer);
        dto.setRecipientOffer(recipientOffer);
        dto.setInitiatorAgreed(trade.isInitiatorAgreed());
        dto.setRecipientAgreed(trade.isRecipientAgreed());

        return dto;
    }

    public List<TradeDetailsDto> getActiveTrades(User user) {
        return tradeRepository.findActiveTradesByUser(user).stream()
                .map(t -> getTradeDetails(t.getId()))
                .collect(Collectors.toList());
    }
}