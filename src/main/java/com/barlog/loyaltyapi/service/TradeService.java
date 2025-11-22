package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.*;
import com.barlog.loyaltyapi.exception.ResourceNotFoundException;
import com.barlog.loyaltyapi.model.CoinTransaction;
import com.barlog.loyaltyapi.model.Trade;

import com.barlog.loyaltyapi.model.TradeOfferItemType;

import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.model.UserInventoryItem;
import com.barlog.loyaltyapi.repository.*;
import jakarta.persistence.EntityExistsException;
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
    private final UserInventoryItemRepository inventoryItemRepository;
    private final UserRepository userRepository;
    private final CoinTransactionRepository coinTransactionRepository;
    private final AdminService adminService;
    private final InventoryService inventoryService;
    private final EntityManager entityManager;

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

    // NOU: Validarea ofertei cu excluderea trade-ului curent
    private void validateOffer(User user, Integer coins, List<Long> itemIds, Long currentTradeId) {
        // 1. Validare monede
        if (coins != null && coins > 0 && user.getCoins() < coins) {
            throw new IllegalStateException("Fonduri insuficiente! Utilizatorul " + user.getNickname() + " nu are " + coins + " monede.");
        }

        // 2. Validare iteme din inventar
        if (itemIds != null && !itemIds.isEmpty()) {
            List<UserInventoryItem> items = inventoryItemRepository.findAllById(itemIds);
            if (items.size() != itemIds.size()) {
                throw new ResourceNotFoundException("Unul sau mai multe iteme din ofertă nu au fost găsite.");
            }

            boolean allOwned = items.stream().allMatch(item -> item.getUser().equals(user) && "IN_INVENTORY".equals(item.getStatus()));
            if (!allOwned) {
                throw new IllegalStateException("Unul sau mai multe iteme nu vă aparțin sau nu sunt disponibile.");
            }

            items.forEach(item -> {
                // Verifică în alte trade-uri active (t.id <> currentTradeId)
                if (tradeRepository.findActiveTradeByItemAndUser(user, item.getId(), currentTradeId).isPresent()) {
                    throw new EntityExistsException("Itemul " + item.getProduct().getName() + " este deja în alt trade activ.");
                }
            });
        }
    }

    // NOU: Salvează/Actualizează itemele în TradeOfferItem
    private void saveOfferItems(Trade trade, User user, Integer coins, List<Long> itemIds) {
        // Șterge orice ofertă veche
        offerItemRepository.deleteByTradeAndUser(trade, user);

        // 1. Salvează Monedele
        if (coins != null && coins > 0) {
            TradeOfferItem coinsOffer = TradeOfferItem.builder()
                    .trade(trade)
                    .user(user)
                    .itemType(TradeOfferItemType.COINS)
                    .offeredAmount(coins)
                    .build();
            offerItemRepository.save(coinsOffer);
        }

        // 2. Salvează Itemele din Inventar
        if (itemIds != null && !itemIds.isEmpty()) {
            List<UserInventoryItem> items = inventoryItemRepository.findAllById(itemIds);
            items.forEach(item -> {
                TradeOfferItem itemOffer = TradeOfferItem.builder()
                        .trade(trade)
                        .user(user)
                        .itemType(TradeOfferItemType.INVENTORY_ITEM)
                        .inventoryItem(item)
                        .build();
                offerItemRepository.save(itemOffer);
            });
        }
    }

    private TradeOfferItemDto mapOfferItemToDto(TradeOfferItem item) {
        TradeOfferItemDto dto = new TradeOfferItemDto();
        dto.setId(item.getId());
        dto.setItemType(item.getItemType());
        dto.setOfferedAmount(item.getOfferedAmount());
        if (item.getInventoryItem() != null) {
            dto.setInventoryItem(inventoryService.mapToDto(item.getInventoryItem()));
        }
        return dto;
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
                    .description("Trade OUT către " + receiver.getNickname())
                    .transactionType("TRADE_OUT")
                    .createdAt(LocalDateTime.now())
                    .build());

            receiver.setCoins(receiver.getCoins() + coinsOffered);
            coinTransactionRepository.save(CoinTransaction.builder()
                    .user(receiver)
                    .amount(coinsOffered)
                    .description("Trade IN de la " + sender.getNickname())
                    .transactionType("TRADE_IN")
                    .createdAt(LocalDateTime.now())
                    .build());

            userRepository.save(sender);
            userRepository.save(receiver);
        }
    }

    private void moveInventoryItems(User sender, List<TradeOfferItemDto> senderOffer, User receiver) {
        senderOffer.stream()
                .filter(item -> item.getItemType() == TradeOfferItemType.INVENTORY_ITEM)
                .forEach(itemDto -> {
                    UserInventoryItem item = inventoryItemRepository.findById(itemDto.getInventoryItem().getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Item de inventar negăsit la finalizare trade."));

                    item.setUser(receiver);
                    inventoryItemRepository.save(item);
                });
    }

    // --- Funcționalități Expuse ---

    @Transactional
    public TradeDetailsDto initiateTrade(User initiator, TradeInitiateRequest request) {
        User recipient = findUserByIdentifier(request.getRecipientIdentifier());

        if (initiator.equals(recipient)) {
            throw new IllegalStateException("Nu poți tranzacționa cu tine însuți.");
        }

        // 0L pentru inițiere, trade-ul nu există încă
        validateOffer(initiator, request.getOfferedCoins(), request.getOfferedInventoryItemIds(), 0L);

        Trade newTrade = Trade.builder()
                .initiator(initiator)
                .recipient(recipient)
                .status(TradeStatus.INITIATED)
                .initiatorAgreed(true)
                .recipientAgreed(false)
                .build();
        Trade savedTrade = tradeRepository.save(newTrade);

        saveOfferItems(savedTrade, initiator, request.getOfferedCoins(), request.getOfferedInventoryItemIds());

        return getTradeDetails(savedTrade.getId());
    }

    @Transactional
    public TradeDetailsDto makeOffer(User user, Long tradeId, TradeOfferRequest request) {
        Trade trade = findActiveTrade(tradeId);

        if (!trade.getInitiator().equals(user) && !trade.getRecipient().equals(user)) {
            throw new IllegalStateException("Nu ești parte în această tranzacție.");
        }

        // TRIMITERE ID: tradeId (pentru a exclude trade-ul curent din verificare)
        validateOffer(user, request.getOfferedCoins(), request.getOfferedInventoryItemIds(), tradeId);

        // 1. Salvează oferta curentă
        saveOfferItems(trade, user, request.getOfferedCoins(), request.getOfferedInventoryItemIds());

        // 2. Determină rolul și actualizează Agreed
        boolean isInitiator = trade.getInitiator().equals(user);

        if (isInitiator) {
            trade.setInitiatorAgreed(request.isAcceptsFinalOffer());
            // Resetează acordul partenerului DOAR dacă initiatorul NU acceptă
            if (!request.isAcceptsFinalOffer()) {
                trade.setRecipientAgreed(false);
            }
        } else {
            trade.setRecipientAgreed(request.isAcceptsFinalOffer());
            // Resetează acordul partenerului DOAR dacă recipientul NU acceptă
            if (!request.isAcceptsFinalOffer()) {
                trade.setInitiatorAgreed(false);
            }
        }

        // 3. Verifică starea finală (CRUCIAL PENTRU CONSENSUALITATE)
        if (trade.isInitiatorAgreed() && trade.isRecipientAgreed()) {
            trade.setStatus(TradeStatus.ACCEPTED);
        } else {
            trade.setStatus(TradeStatus.PENDING_APPROVAL);
        }

        tradeRepository.save(trade);
        return getTradeDetails(tradeId);
    }

    @Transactional
    public void cancelTrade(User user, Long tradeId) {
        Trade trade = findActiveTrade(tradeId);

        if (!trade.getInitiator().equals(user) && !trade.getRecipient().equals(user)) {
            throw new IllegalStateException("Nu poți anula o tranzacție în care nu ești parte.");
        }

        if (trade.getStatus() == TradeStatus.COMPLETED) {
            throw new IllegalStateException("Tranzacția a fost deja finalizată și nu poate fi anulată.");
        }

        offerItemRepository.deleteByTradeAndUser(trade, trade.getInitiator());
        offerItemRepository.deleteByTradeAndUser(trade, trade.getRecipient());
        trade.setStatus(TradeStatus.CANCELED);
        tradeRepository.save(trade);
    }

    @Transactional
    public TradeDetailsDto completeTrade(User user, Long tradeId) {
        Trade trade = entityManager.find(Trade.class, tradeId, LockModeType.PESSIMISTIC_WRITE);

        if (trade == null) {
            throw new ResourceNotFoundException("Tranzacția nu a fost găsită.");
        }
        if (!trade.getInitiator().equals(user) && !trade.getRecipient().equals(user)) {
            throw new IllegalStateException("Nu ești parte în această tranzacție.");
        }
        if (trade.getStatus() != TradeStatus.ACCEPTED) {
            throw new IllegalStateException("Tranzacția nu este ACCEPTED și nu poate fi finalizată. Status: " + trade.getStatus().name());
        }

        TradeDetailsDto details = getTradeDetails(tradeId);

        User initiatorEntity = userRepository.findById(details.getInitiator().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Initiator user not found"));
        User recipientEntity = userRepository.findById(details.getRecipient().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient user not found"));

        processUserTrade(initiatorEntity, details.getInitiatorOffer(), recipientEntity);
        processUserTrade(recipientEntity, details.getRecipientOffer(), initiatorEntity);

        moveInventoryItems(initiatorEntity, details.getInitiatorOffer(), recipientEntity);
        moveInventoryItems(recipientEntity, details.getRecipientOffer(), initiatorEntity);

        trade.setStatus(TradeStatus.COMPLETED);
        tradeRepository.save(trade);

        return getTradeDetails(tradeId);
    }

    @Transactional
    public TradeDetailsDto getTradeDetails(Long tradeId) {
        Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Tranzacția nu a fost găsită."));

        List<TradeOfferItem> allOffers = offerItemRepository.findByTrade(trade);

        List<TradeOfferItemDto> initiatorOffer = allOffers.stream()
                .filter(item -> item.getUser().equals(trade.getInitiator()))
                .map(this::mapOfferItemToDto)
                .collect(Collectors.toList());

        List<TradeOfferItemDto> recipientOffer = allOffers.stream()
                .filter(item -> item.getUser().equals(trade.getRecipient()))
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