package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.ProductResponseDto;
import com.barlog.loyaltyapi.dto.QuestCreateDto;
import com.barlog.loyaltyapi.dto.QuestCriterionDto;
import com.barlog.loyaltyapi.dto.QuestDetailsDto;
import com.barlog.loyaltyapi.dto.UserCriterionProgressDto;
import com.barlog.loyaltyapi.dto.UserQuestLogDto;
import com.barlog.loyaltyapi.exception.ResourceNotFoundException;
import com.barlog.loyaltyapi.model.*;
import com.barlog.loyaltyapi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestService {

    private final QuestRepository questRepository;
    private final UserQuestLogRepository questLogRepository;
    private final UserCriterionProgressRepository criterionProgressRepository;
    private final InventoryService inventoryService;
    private final ExperienceService experienceService;
    private final ProductRepository productRepository; // Pentru Admin CRUD
    private final UserRepository userRepository;
    private final CoinTransactionRepository coinTransactionRepository;
    // --- Mappers ---

    private QuestCriterion mapToEntity(QuestCriterionDto dto, Quest quest) {
        // Logica de mapare simplificată pentru a găsi Produsul țintă
        Product targetProduct = dto.getTargetProductId() != null
                ? productRepository.findById(dto.getTargetProductId()).orElse(null)
                : null;

        return QuestCriterion.builder()
                .quest(quest)
                .criterionType(dto.getCriterionType())
                .targetCategory(dto.getTargetCategory())
                .targetProduct(targetProduct)
                .requiredAmount(dto.getRequiredAmount())
                .build();
    }

    private QuestCriterionDto mapToDto(QuestCriterion entity) {
        QuestCriterionDto dto = new QuestCriterionDto();
        dto.setCriterionType(entity.getCriterionType());
        dto.setTargetCategory(entity.getTargetCategory());
        dto.setTargetProductId(entity.getTargetProduct() != null ? entity.getTargetProduct().getId() : null);
        dto.setRequiredAmount(entity.getRequiredAmount());
        return dto;
    }

    private QuestDetailsDto mapToDetailsDto(Quest quest) {
        QuestDetailsDto dto = new QuestDetailsDto();
        dto.setId(quest.getId());
        dto.setTitle(quest.getTitle());
        dto.setDescription(quest.getDescription());
        dto.setDurationDays(quest.getDurationDays());
        dto.setType(quest.getType());
        dto.setRewardCoins(quest.getRewardCoins());
        dto.setRewardXp(quest.getRewardXp());
        dto.setRewardProductId(quest.getRewardProduct() != null ? quest.getRewardProduct().getId() : null);
        dto.setRewardProductName(quest.getRewardProduct() != null ? quest.getRewardProduct().getName() : null);
        dto.setActive(quest.isActive());
        dto.setCreatedAt(quest.getCreatedAt());
        dto.setCriteria(quest.getCriteria().stream().map(this::mapToDto).collect(Collectors.toList()));
        return dto;
    }

    private UserQuestLogDto mapToLogDto(UserQuestLog log) {
        UserQuestLogDto dto = new UserQuestLogDto();
        dto.setId(log.getId());
        dto.setQuestId(log.getQuest().getId());
        dto.setTitle(log.getQuest().getTitle());
        dto.setDescription(log.getQuest().getDescription());
        dto.setStatus(log.getStatus().name());
        dto.setStartDate(log.getStartDate());
        dto.setCompletionDate(log.getCompletionDate());

        // Mapează progresul criteriilor
        dto.setCriterionProgress(log.getCriterionProgress().stream()
                .map(this::mapProgressToDto)
                .collect(Collectors.toList()));

        return dto;
    }

    private UserCriterionProgressDto mapProgressToDto(UserCriterionProgress progress) {
        UserCriterionProgressDto dto = new UserCriterionProgressDto();
        QuestCriterion criterion = progress.getCriterion();

        dto.setCriterionId(criterion.getId());
        dto.setCurrentProgress(progress.getCurrentProgress());
        dto.setRequiredAmount(criterion.getRequiredAmount());
        dto.setIsCompleted(progress.getIsCompleted());

        // Calculează descrierea prietenoasă
        String targetName = criterion.getTargetProduct() != null
                ? criterion.getTargetProduct().getName()
                : criterion.getTargetCategory() != null ? criterion.getTargetCategory().name() : "Monede";

        dto.setCriterionDescription(String.format(
                "%s: %s necesar(e) din %s",
                criterion.getCriterionType().name().replace("_", " "),
                String.valueOf(criterion.getRequiredAmount()),
                targetName
        ));

        dto.setProgressPercentage((int) Math.min(100, (progress.getCurrentProgress() / criterion.getRequiredAmount()) * 100));

        return dto;
    }

    // --- Admin CRUD ---

    @Transactional
    public QuestDetailsDto createQuest(QuestCreateDto createDto) {
        Product rewardProduct = createDto.getRewardProductId() != null
                ? productRepository.findById(createDto.getRewardProductId()).orElse(null)
                : null;

        Quest quest = Quest.builder()
                .title(createDto.getTitle())
                .description(createDto.getDescription())
                .durationDays(createDto.getDurationDays())
                // Tipul principal al Quest-ului (pentru frontend)
                .type(QuestType.valueOf(createDto.getType()))
                .rewardCoins(createDto.getRewardCoins())
                .rewardXp(createDto.getRewardXp())
                .rewardProduct(rewardProduct)
                .isActive(createDto.isActive())
                .build();

        Quest savedQuest = questRepository.save(quest);

        // Salvează Criteriile separat
        List<QuestCriterion> criteria = createDto.getCriteria().stream()
                .map(dto -> {
                    QuestCriterion criterion = mapToEntity(dto, savedQuest);
                    criterion.setQuest(savedQuest); // Asigură referința bidirecțională
                    return criterion;
                })
                .collect(Collectors.toList());

        // Aici trebuie să salvezi și criteriile, deși CascadeType.ALL le-ar putea salva automat.
        // Vom avea nevoie de un repository separat pentru criteriile dacă Cascade nu funcționează.
        // Simplificăm prin setarea listei pe Quest și re-salvare.
        savedQuest.setCriteria(criteria);
        // questRepository.save(savedQuest); // Re-salvarea cu Cascade va salva criteriile

        return mapToDetailsDto(savedQuest);
    }

    public List<QuestDetailsDto> getAllQuestsForAdmin() {
        return questRepository.findAll().stream()
                .map(this::mapToDetailsDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public QuestDetailsDto updateQuest(Long questId, QuestCreateDto updateDto) {
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new ResourceNotFoundException("Quest-ul nu a fost găsit."));

        Product rewardProduct = updateDto.getRewardProductId() != null
                ? productRepository.findById(updateDto.getRewardProductId()).orElse(null)
                : null;

        // Aplică modificările
        quest.setTitle(updateDto.getTitle());
        quest.setDescription(updateDto.getDescription());
        quest.setDurationDays(updateDto.getDurationDays());
        quest.setType(QuestType.valueOf(updateDto.getType()));
        quest.setRewardCoins(updateDto.getRewardCoins());
        quest.setRewardXp(updateDto.getRewardXp());
        quest.setRewardProduct(rewardProduct);
        quest.setActive(updateDto.isActive());

        // TODO: Aici ar trebui implementată și logica de ștergere/recreare a criteriilor vechi.

        Quest updatedQuest = questRepository.save(quest);
        return mapToDetailsDto(updatedQuest);
    }

    // --- User Flow ---

    @Transactional
    public void assignActiveQuests(User user) {
        // ... (Logica de asignare - Neschimbată)
        List<Quest> activeQuests = questRepository.findAllByIsActiveTrue();

        for (Quest quest : activeQuests) {
            boolean alreadyActive = questLogRepository
                    .findByUserAndQuestIdAndStatus(user, quest.getId(), QuestStatus.ACTIVE)
                    .isPresent();

            if (!alreadyActive) {
                UserQuestLog newLog = UserQuestLog.builder()
                        .user(user)
                        .quest(quest)
                        .status(QuestStatus.ACTIVE)
                        .startDate(LocalDate.now())
                        .build();
                UserQuestLog savedLog = questLogRepository.save(newLog);

                initializeCriterionProgress(savedLog, quest.getCriteria());
            }
        }
    }

    private void initializeCriterionProgress(UserQuestLog log, List<QuestCriterion> criteria) {
        criteria.forEach(criterion -> {
            UserCriterionProgress progress = UserCriterionProgress.builder()
                    .userQuestLog(log)
                    .criterion(criterion)
                    .currentProgress(0.0)
                    .isCompleted(false)
                    .uniqueKey(log.getId() + "_" + criterion.getId())
                    .build();
            criterionProgressRepository.save(progress);
        });
    }

    @Transactional
    public void updateQuestProgress(User user, QuestType eventType, ProductCategory category, Long productId, double amount) {

        // 1. Preia toate log-urile ACTIVE
        List<UserQuestLog> logsWithDetails = questLogRepository.findAllByUserAndStatusInForDisplay(user);

        List<UserQuestLog> activeLogs = logsWithDetails.stream()
                .filter(log -> log.getStatus() == QuestStatus.ACTIVE)
                .collect(Collectors.toList());

        for (UserQuestLog log : activeLogs) {
            boolean allCriteriaMet = true;

            if (log.getStatus() != QuestStatus.ACTIVE) {
                continue; // Sărim peste Completed/Rewarded
            }

            for (QuestCriterion criterion : log.getQuest().getCriteria()) {

                UserCriterionProgress progress = log.getCriterionProgress().stream()
                        .filter(p -> p.getCriterion().equals(criterion))
                        .findFirst()
                        .orElse(null);

                if (progress == null || progress.getIsCompleted()) {
                    if (progress == null) allCriteriaMet = false; // Dacă progresul lipsește, nu e complet
                    continue;
                }

                // A. Dacă tipul event-ului se potrivește
                if (criterion.getCriterionType() == eventType) {

                    // B. Verifică detaliile specifice
                    boolean matches = false;
                    if (eventType == QuestType.BUY_PRODUCT_CATEGORY && criterion.getTargetCategory() == category) {
                        matches = true;
                    } else if (eventType == QuestType.BUY_SPECIFIC_PRODUCT && criterion.getTargetProduct() != null && criterion.getTargetProduct().getId().equals(productId)) {
                        matches = true;
                    } else if (eventType == QuestType.GAIN_COINS) {
                        matches = true;
                    }

                    // C. Actualizează progresul
                    if (matches) {
                        progress.setCurrentProgress(Math.min(progress.getCurrentProgress() + amount, criterion.getRequiredAmount()));
                        if (progress.getCurrentProgress() >= criterion.getRequiredAmount()) {
                            progress.setIsCompleted(true);
                        }
                        criterionProgressRepository.save(progress);
                    }
                }

                // D. Verifică dacă toate criteriile sunt îndeplinite pentru acest Quest Log
                if (!progress.getIsCompleted()) {
                    allCriteriaMet = false;
                }
            }

            // 3. Dacă toate criteriile au fost îndeplinite, marchează Quest-ul ca COMPLETED
            if (allCriteriaMet) {
                log.setStatus(QuestStatus.COMPLETED);
                log.setCompletionDate(LocalDateTime.now());
                questLogRepository.save(log);
            }
        }
    }

    @Transactional
    public UserQuestLogDto claimReward(User user, Long userQuestLogId) {
        UserQuestLog log = questLogRepository.findById(userQuestLogId)
                .orElseThrow(() -> new ResourceNotFoundException("Jurnal de Quest negăsit."));

        if (!log.getUser().equals(user)) {
            throw new ResourceNotFoundException("Nu ești proprietarul acestui Quest Log.");
        }
        if (log.getStatus() != QuestStatus.COMPLETED) {
            throw new IllegalStateException("Quest-ul nu este completat și nu poate fi revendicat.");
        }

        Quest quest = log.getQuest();

        // 1. Acordă Monede (LOGICA MUTATĂ AICI)
        if (quest.getRewardCoins() != null && quest.getRewardCoins() > 0) {
            int coins = quest.getRewardCoins();
            String description = "Recompensă Quest: " + quest.getTitle();

            // Logica adăugată:
            user.setCoins(user.getCoins() + coins);

            coinTransactionRepository.save(CoinTransaction.builder()
                    .user(user)
                    .amount(coins)
                    .description(description)
                    .transactionType("QUEST_REWARD") // Tip Tranzacție
                    .createdAt(LocalDateTime.now())
                    .build());
            userRepository.save(user); // Salvează balanța nouă
        }

        // 2. Acordă XP (rămâne no-op aici, XP este deja tratat corect)
        if (quest.getRewardXp() != null && quest.getRewardXp() > 0.0) {
            experienceService.addExperience(user, quest.getRewardXp(), "QUEST_REWARD", null, "Recompensă Quest: " + quest.getTitle());
        }

        // 3. Acordă Item (rămâne a fi implementat, já está com InventoryService)
        if (quest.getRewardProduct() != null) {
            Product rewardProduct = quest.getRewardProduct();
            UserInventoryItem newItem = UserInventoryItem.builder()
                    .user(user)
                    .product(rewardProduct)
                    .status("IN_INVENTORY")
                    .build();
            inventoryService.saveInventoryItem(newItem);
        }

        // 4. Marchează ca REWARDED
        log.setStatus(QuestStatus.REWARDED);
        UserQuestLog savedLog = questLogRepository.save(log);

        return mapToLogDto(savedLog);
    }
    public List<UserQuestLogDto> getUserQuestLog(User user) {
        // Asigură-te că UserQuestLog este populat cu detalii (JOIN FETCH)
        List<UserQuestLog> logs = questLogRepository.findUserQuestsWithDetails(user);

        // Asigură-te că sunt returnate doar cele Active, Completed și REWARDED (pentru istoric)
        return logs.stream()
                .filter(log -> log.getStatus() != QuestStatus.REWARDED || log.getCompletionDate().isAfter(LocalDateTime.now().minusDays(30))) // Ex. păstrează istoricul 30 de zile
                .map(this::mapToLogDto)
                .collect(Collectors.toList());
    }
}