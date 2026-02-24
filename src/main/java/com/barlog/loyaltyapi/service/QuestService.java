package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.*;
import com.barlog.loyaltyapi.exception.ResourceNotFoundException;
import com.barlog.loyaltyapi.model.*;
import com.barlog.loyaltyapi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestService {

    private final QuestRepository questRepository;
    private final UserQuestLogRepository questLogRepository;
    private final UserCriterionProgressRepository criterionProgressRepository;
    private final QuestCriterionRepository questCriterionRepository;

    // Servicii externe
    private final InventoryService inventoryService;
    private final ExperienceService experienceService;
    private final BonusService bonusService;
    private final ProductService productService;
    private final UserNotificationService notificationService;

    // Repositoare externe
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CoinTransactionRepository coinTransactionRepository;
    private final ItemTemplateRepository itemTemplateRepository;
    private final UserItemRepository userItemRepository;

    // =================================================================================
    // MAPPERS (DTO <-> Entity)
    // =================================================================================

    private QuestCriterion mapToEntity(QuestCriterionDto dto, Quest quest) {
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

    // Mapare pentru Admin (Editare/Vizualizare)
    private QuestDetailsDto mapToDetailsDto(Quest quest) {
        QuestDetailsDto dto = new QuestDetailsDto();
        dto.setId(quest.getId());
        dto.setTitle(quest.getTitle());
        dto.setDescription(quest.getDescription());
        dto.setDurationDays(quest.getDurationDays());
        dto.setType(quest.getType());
        dto.setRewardCoins(quest.getRewardCoins());
        dto.setRewardXp(quest.getRewardXp());

        // Produs Fizic
        dto.setRewardProductId(quest.getRewardProduct() != null ? quest.getRewardProduct().getId() : null);
        dto.setRewardProductName(quest.getRewardProduct() != null ? quest.getRewardProduct().getName() : null);

        // Item RPG
        dto.setRewardItemTemplateId(quest.getRewardItemTemplate() != null ? quest.getRewardItemTemplate().getId() : null);

        dto.setActive(quest.isActive());
        dto.setCreatedAt(quest.getCreatedAt());
        dto.setCriteria(quest.getCriteria().stream().map(this::mapToDto).collect(Collectors.toList()));
        return dto;
    }

    // Mapare pentru User (Log-ul personal)
    private UserQuestLogDto mapToLogDto(UserQuestLog log) {
        UserQuestLogDto dto = new UserQuestLogDto();
        dto.setId(log.getId());
        dto.setQuestId(log.getQuest().getId());
        dto.setTitle(log.getQuest().getTitle());
        dto.setDescription(log.getQuest().getDescription());
        dto.setStatus(log.getStatus().name());
        dto.setStartDate(log.getStartDate());
        dto.setCompletionDate(log.getCompletionDate());

        Quest quest = log.getQuest();
        dto.setRewardCoins(quest.getRewardCoins());
        dto.setRewardXp(quest.getRewardXp());
        dto.setRewardProductName(quest.getRewardProduct() != null ? quest.getRewardProduct().getName() : null);

        // Setăm numele item-ului RPG recompensă pentru afișare în UI
        dto.setRewardItemName(quest.getRewardItemTemplate() != null ? quest.getRewardItemTemplate().getName() : null);

        dto.setCriterionProgress(log.getCriterionProgress().stream()
                .map(this::mapProgressToDto)
                .collect(Collectors.toList()));

        return dto;
    }

    private UserCriterionProgressDto mapProgressToDto(UserCriterionProgress progress) {
        UserCriterionProgressDto dto = new UserCriterionProgressDto();
        QuestCriterion criterion = progress.getCriterion();

        dto.setCriterionId(criterion.getId());
        dto.setCriterionType(criterion.getCriterionType().name());
        dto.setCurrentProgress(progress.getCurrentProgress());
        dto.setRequiredAmount(criterion.getRequiredAmount());
        dto.setIsCompleted(progress.getIsCompleted());

        String targetName = criterion.getTargetProduct() != null
                ? criterion.getTargetProduct().getName()
                : criterion.getTargetCategory() != null ? criterion.getTargetCategory().name() : "Monede";

        dto.setCriterionDescription(String.format(
                "%s: %s necesar(e) din %s",
                criterion.getCriterionType().name().replace("_", " "),
                String.valueOf(criterion.getRequiredAmount()),
                targetName
        ));

        // Calcul procent (max 100%)
        double percentage = (progress.getCurrentProgress() / criterion.getRequiredAmount()) * 100;
        dto.setProgressPercentage((int) Math.min(100, percentage));

        return dto;
    }

    // =================================================================================
    // ADMIN ACTIONS (CRUD)
    // =================================================================================

    @Transactional
    public QuestDetailsDto createQuest(QuestCreateDto createDto) {
        Product rewardProduct = createDto.getRewardProductId() != null
                ? productRepository.findById(createDto.getRewardProductId()).orElse(null)
                : null;

        ItemTemplate rewardItem = null;
        if (createDto.getRewardItemTemplateId() != null) {
            rewardItem = itemTemplateRepository.findById(createDto.getRewardItemTemplateId()).orElse(null);
        }

        Quest quest = Quest.builder()
                .title(createDto.getTitle())
                .description(createDto.getDescription())
                .durationDays(createDto.getDurationDays())
                .type(QuestType.valueOf(createDto.getType()))
                .rewardCoins(createDto.getRewardCoins())
                .rewardXp(createDto.getRewardXp())
                .rewardProduct(rewardProduct)
                .rewardItemTemplate(rewardItem) // Setare Item RPG
                .isActive(createDto.isActive())
                .build();

        Quest savedQuest = questRepository.save(quest);

        Set<QuestCriterion> criteria = createDto.getCriteria().stream()
                .map(dto -> {
                    QuestCriterion criterion = mapToEntity(dto, savedQuest);
                    criterion.setQuest(savedQuest);
                    return questCriterionRepository.save(criterion);
                })
                .collect(Collectors.toSet());

        savedQuest.setCriteria(criteria);

        // Atribuie quest-ul utilizatorilor existenți
        assignQuestToAllUsers(savedQuest);

        return mapToDetailsDto(savedQuest);
    }

    @Transactional
    public QuestDetailsDto updateQuest(Long questId, QuestCreateDto updateDto) {
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new ResourceNotFoundException("Quest-ul nu a fost găsit."));

        // 1. Actualizare câmpuri de bază
        quest.setTitle(updateDto.getTitle());
        quest.setDescription(updateDto.getDescription());
        quest.setDurationDays(updateDto.getDurationDays());
        quest.setType(QuestType.valueOf(updateDto.getType()));
        quest.setRewardCoins(updateDto.getRewardCoins());
        quest.setRewardXp(updateDto.getRewardXp());

        // Update reward product
        if (updateDto.getRewardProductId() != null) {
            Product rewardP = productRepository.findById(updateDto.getRewardProductId()).orElse(null);
            quest.setRewardProduct(rewardP);
        } else {
            quest.setRewardProduct(null);
        }

        // Update reward Item RPG
        if (updateDto.getRewardItemTemplateId() != null) {
            ItemTemplate rewardI = itemTemplateRepository.findById(updateDto.getRewardItemTemplateId()).orElse(null);
            quest.setRewardItemTemplate(rewardI);
        } else {
            quest.setRewardItemTemplate(null);
        }

        quest.setActive(updateDto.isActive());

        // 2. Gestionare Criterii (Ștergere vechi + Adăugare noi)
        // PAS A: Curățăm orice progres al utilizatorilor legat de criteriile ACESTUI quest.
        List<QuestCriterion> currentCriteria = List.copyOf(quest.getCriteria());
        for (QuestCriterion oldCriterion : currentCriteria) {
            criterionProgressRepository.deleteByCriterion(oldCriterion);
        }
        criterionProgressRepository.flush();

        // PAS B: Golim colecția părintelui
        quest.getCriteria().clear();
        questRepository.flush();

        // PAS C: Adăugăm noile criterii
        if (updateDto.getCriteria() != null) {
            for (QuestCriterionDto dto : updateDto.getCriteria()) {
                QuestCriterion criterion = mapToEntity(dto, quest);
                criterion.setQuest(quest);
                quest.getCriteria().add(criterion);
            }
        }

        Quest updatedQuest = questRepository.save(quest);

        // 3. Reactivare și Re-inițializare Progres pentru useri
        if (updatedQuest.isActive()) {
            // A. Reactivare quest-uri expirate
            List<UserQuestLog> expiredLogs = questLogRepository.findByQuestIdAndStatus(questId, QuestStatus.EXPIRED);
            for (UserQuestLog log : expiredLogs) {
                log.setStatus(QuestStatus.ACTIVE);
                log.setCompletionDate(null);
                questLogRepository.save(log);
                initializeCriterionProgress(log, updatedQuest.getCriteria());

                notificationService.notifyUser(
                        log.getUser(),
                        "Quest Actualizat: " + updatedQuest.getTitle() + " este din nou disponibil!",
                        NotificationType.SYSTEM,
                        "/quests"
                );
            }

            // B. Resetare quest-uri active (din cauza schimbării criteriilor)
            List<UserQuestLog> activeLogs = questLogRepository.findByQuestIdAndStatus(questId, QuestStatus.ACTIVE);
            for (UserQuestLog log : activeLogs) {
                initializeCriterionProgress(log, updatedQuest.getCriteria());

                notificationService.notifyUser(
                        log.getUser(),
                        "Quest Modificat: Obiectivele pentru '" + updatedQuest.getTitle() + "' s-au schimbat.",
                        NotificationType.SYSTEM,
                        "/quests"
                );
            }
        }

        return mapToDetailsDto(updatedQuest);
    }

    @Transactional
    public void deleteQuest(Long questId) {
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new ResourceNotFoundException("Quest-ul cu ID-ul " + questId + " nu a fost găsit."));

        // 1. Dezactivează definiția Quest-ului
        quest.setActive(false);
        questRepository.save(quest);

        // 2. Marchează log-urile userilor ca EXPIRED
        List<UserQuestLog> activeUserLogs = questLogRepository.findByQuestIdAndStatus(questId, QuestStatus.ACTIVE);
        for (UserQuestLog log : activeUserLogs) {
            log.setStatus(QuestStatus.EXPIRED);
            log.setCompletionDate(LocalDateTime.now());
            questLogRepository.save(log);
        }
    }

    public List<QuestDetailsDto> getAllQuestsForAdmin() {
        return questRepository.findAllWithCriteria().stream()
                .map(this::mapToDetailsDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void adminForceCompleteQuest(String userEmail, Long questId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizator negăsit: " + userEmail));

        UserQuestLog log = questLogRepository.findByUserAndQuestIdAndStatus(user, questId, QuestStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Acest utilizator nu are quest-ul activ (sau e deja completat)."));

        log.setStatus(QuestStatus.COMPLETED);
        log.setCompletionDate(LocalDateTime.now());
        questLogRepository.save(log);

        notificationService.notifyUser(
                user,
                "Quest Completat de Admin: " + log.getQuest().getTitle() + ". Intră să revendici recompensa!",
                NotificationType.QUEST_COMPLETED,
                "/quests"
        );
    }

    // =================================================================================
    // USER ACTIONS & LOGIC
    // =================================================================================

    @Transactional
    public void assignActiveQuests(User user) {
        List<Quest> activeQuests = questRepository.findAllByIsActiveTrue();

        for (Quest quest : activeQuests) {
            boolean alreadyActive = questLogRepository
                    .findByUserAndQuestIdAndStatus(user, quest.getId(), QuestStatus.ACTIVE)
                    .isPresent();

            if (!alreadyActive) {
               // assignQuestToAllUsers(quest); // Folosim metoda generică, dar limitat la acest user în logică
                // Mai eficient:
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

    @Transactional
    public void assignQuestToAllUsers(Quest newQuest) {
        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            boolean alreadyActive = questLogRepository
                    .findByUserAndQuestIdAndStatus(user, newQuest.getId(), QuestStatus.ACTIVE)
                    .isPresent();

            if (!alreadyActive) {
                UserQuestLog newLog = UserQuestLog.builder()
                        .user(user)
                        .quest(newQuest)
                        .status(QuestStatus.ACTIVE)
                        .startDate(LocalDate.now())
                        .build();
                UserQuestLog savedLog = questLogRepository.save(newLog);

                initializeCriterionProgress(savedLog, newQuest.getCriteria());

                notificationService.notifyUser(
                        user,
                        "Quest Nou Disponibil: " + newQuest.getTitle(),
                        NotificationType.SYSTEM,
                        "/quests"
                );
            }
        }
    }

    private void initializeCriterionProgress(UserQuestLog log, Collection<QuestCriterion> criteria) {
        Long logId = log.getId();

        criteria.forEach(criterion -> {
            UserCriterionProgress progress = UserCriterionProgress.builder()
                    .userQuestLog(log)
                    .criterion(criterion)
                    .currentProgress(0.0)
                    .isCompleted(false)
                    .uniqueKey(logId + "_" + criterion.getId())
                    .build();
            criterionProgressRepository.save(progress);
        });
    }

    public List<UserQuestLogDto> getUserQuestLog(User user) {
        List<UserQuestLog> logs = questLogRepository.findAllByUserAndStatusInForDisplay(user);

        return logs.stream()
                // Filtrăm REWARDED foarte vechi, dar păstrăm EXPIRED
                .filter(log -> log.getStatus() != QuestStatus.REWARDED || log.getCompletionDate().isAfter(LocalDateTime.now().minusDays(30)))
                .map(log -> {
                    if (log.getStatus() == QuestStatus.EXPIRED) {
                        return mapToLogDto(log);
                    }

                    // Logica de calcul progres "On-Demand"
                    Set<UserCriterionProgress> currentProgressSet = log.getCriterionProgress();
                    if (currentProgressSet == null || currentProgressSet.isEmpty()) {
                        initializeCriterionProgress(log, log.getQuest().getCriteria());
                        currentProgressSet = log.getCriterionProgress();
                    }

                    for (UserCriterionProgress p : currentProgressSet) {
                        if (!p.getIsCompleted()) {
                            // Calculăm progresul din istoric
                            double historyProgress = calculateProgressFromHistory(user, p.getCriterion(), log.getStartDate().atStartOfDay());

                            if (historyProgress > p.getCurrentProgress()) {
                                p.setCurrentProgress(Math.min(historyProgress, p.getCriterion().getRequiredAmount()));
                                p.setIsCompleted(historyProgress >= p.getCriterion().getRequiredAmount());
                                criterionProgressRepository.save(p);
                            }
                        }
                    }

                    boolean allMet = !currentProgressSet.isEmpty() && currentProgressSet.stream().allMatch(UserCriterionProgress::getIsCompleted);

                    if (allMet && log.getStatus() == QuestStatus.ACTIVE) {
                        log.setStatus(QuestStatus.COMPLETED);
                        log.setCompletionDate(LocalDateTime.now());
                        questLogRepository.save(log);

                        notificationService.notifyUser(
                                user,
                                "QUEST COMPLETAT: " + log.getQuest().getTitle() + "! Revendică recompensa acum.",
                                NotificationType.QUEST_COMPLETED,
                                "/quests"
                        );
                    }

                    return mapToLogDto(log);
                })
                .collect(Collectors.toList());
    }

    private double calculateProgressFromHistory(User user, QuestCriterion criterion, LocalDateTime startTime) {
        if (criterion.getCriterionType() == QuestType.BUY_PRODUCT_CATEGORY || criterion.getCriterionType() == QuestType.BUY_SPECIFIC_PRODUCT) {

            List<CoinTransaction> transactions = coinTransactionRepository.findPhysicalPurchasesByUserAfterDate(user, startTime);

            return transactions.stream()
                    .filter(ct -> {
                        Product product = productService.matchProductByFormattedDescription(ct.getDescription());
                        if (product == null) return false;

                        if (criterion.getTargetProduct() != null) {
                            return product.getId().equals(criterion.getTargetProduct().getId());
                        }
                        if (criterion.getTargetCategory() != null) {
                            return product.getCategory() == criterion.getTargetCategory();
                        }
                        return false;
                    })
                    .count();

        } else if (criterion.getCriterionType() == QuestType.GAIN_COINS) {

            List<CoinTransaction> transactions = coinTransactionRepository.findNetCoinGainsByUserAfterDate(user, startTime);
            return transactions.stream().mapToInt(CoinTransaction::getAmount).sum();
        }

        return 0.0;
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
        double questMultiplier = bonusService.calculateMultiplier(user, ItemEffectType.QUEST_REWARD_BOOST, null);

        // A. Monede
        if (quest.getRewardCoins() != null && quest.getRewardCoins() > 0) {
            int baseCoins = quest.getRewardCoins();
            int finalCoins = (int) Math.round(baseCoins * questMultiplier);

            String desc = "Recompensă Quest: " + quest.getTitle();
            if (questMultiplier > 1.0) desc += " (Bonus Echipament)";

            user.setCoins(user.getCoins() + finalCoins);

            coinTransactionRepository.save(CoinTransaction.builder()
                    .user(user)
                    .amount(finalCoins)
                    .description(desc)
                    .transactionType("QUEST_REWARD")
                    .createdAt(LocalDateTime.now())
                    .build());
            userRepository.save(user);
        }

        // B. XP
        if (quest.getRewardXp() != null && quest.getRewardXp() > 0.0) {
            double finalXp = quest.getRewardXp() * questMultiplier;
            experienceService.addExperience(user, finalXp, "QUEST_REWARD", null, "Recompensă Quest: " + quest.getTitle());
        }

        // C. Produs Fizic
        if (quest.getRewardProduct() != null) {
            UserInventoryItem newItem = UserInventoryItem.builder()
                    .user(user)
                    .product(quest.getRewardProduct())
                    .status("IN_INVENTORY")
                    .build();
            inventoryService.saveInventoryItem(newItem);
        }

        // D. Item RPG (NOU)
        if (quest.getRewardItemTemplate() != null) {
            UserItem newItem = UserItem.builder()
                    .user(user)
                    .itemTemplate(quest.getRewardItemTemplate())
                    .isEquipped(false)
                    .build();
            userItemRepository.save(newItem);

            notificationService.notifyUser(user, "Ai primit un item special: " + quest.getRewardItemTemplate().getName(), NotificationType.SYSTEM, "/character");
        }

        log.setStatus(QuestStatus.REWARDED);
        return mapToLogDto(questLogRepository.save(log));
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireOverdueQuests() {
        System.out.println("--- CRON: Checking for expired quests... ---");

        List<UserQuestLog> activeLogs = questLogRepository.findAllActiveLogsWithQuest();
        LocalDate today = LocalDate.now();
        int expiredCount = 0;

        for (UserQuestLog log : activeLogs) {
            int duration = log.getQuest().getDurationDays();
            LocalDate expirationDate = log.getStartDate().plusDays(duration);

            if (today.isAfter(expirationDate)) {
                log.setStatus(QuestStatus.EXPIRED);
                log.setCompletionDate(LocalDateTime.now());
                questLogRepository.save(log);
                expiredCount++;
            }
        }
        System.out.println("--- CRON: Expired " + expiredCount + " quests. ---");
    }
}