package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.QuestCreateDto;
import com.barlog.loyaltyapi.dto.QuestCriterionDto;
import com.barlog.loyaltyapi.dto.QuestDetailsDto;
import com.barlog.loyaltyapi.dto.UserCriterionProgressDto;
import com.barlog.loyaltyapi.dto.UserQuestLogDto;
import com.barlog.loyaltyapi.exception.ResourceNotFoundException;
import com.barlog.loyaltyapi.model.*;
import com.barlog.loyaltyapi.repository.CoinTransactionRepository;
import com.barlog.loyaltyapi.repository.QuestRepository;
import com.barlog.loyaltyapi.repository.UserCriterionProgressRepository;
import com.barlog.loyaltyapi.repository.UserQuestLogRepository;
import com.barlog.loyaltyapi.repository.ProductRepository;
import com.barlog.loyaltyapi.repository.UserRepository;
import com.barlog.loyaltyapi.repository.QuestCriterionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set; // IMPORT IMPORTANT
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestService {

    private final QuestRepository questRepository;
    private final UserQuestLogRepository questLogRepository;
    private final UserCriterionProgressRepository criterionProgressRepository;
    private final InventoryService inventoryService;
    private final ExperienceService experienceService;
    private final ProductRepository productRepository;
    private final QuestCriterionRepository questCriterionRepository;

    // DEPENDENȚE PENTRU A RUPE CICLUL
    private final ProductService productService;
    private final UserRepository userRepository;
    private final CoinTransactionRepository coinTransactionRepository;
    private final UserNotificationService notificationService; // INJECTAT
    // --- Mappers ---

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
        // Conversie Set -> List pentru DTO
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

        // Maparea Recompenselor
        Quest quest = log.getQuest();
        dto.setRewardCoins(quest.getRewardCoins());
        dto.setRewardXp(quest.getRewardXp());
        dto.setRewardProductName(quest.getRewardProduct() != null ? quest.getRewardProduct().getName() : null);

        // Mapează progresul (Set -> List pentru DTO)
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
                .type(QuestType.valueOf(createDto.getType()))
                .rewardCoins(createDto.getRewardCoins())
                .rewardXp(createDto.getRewardXp())
                .rewardProduct(rewardProduct)
                .isActive(createDto.isActive())
                .build();

        Quest savedQuest = questRepository.save(quest);

        // CORECTAT: Folosim Set și Collectors.toSet()
        Set<QuestCriterion> criteria = createDto.getCriteria().stream()
                .map(dto -> {
                    QuestCriterion criterion = mapToEntity(dto, savedQuest);
                    criterion.setQuest(savedQuest);
                    return questCriterionRepository.save(criterion);
                })
                .collect(Collectors.toSet());

        savedQuest.setCriteria(criteria);

        assignQuestToAllUsers(savedQuest);

        return mapToDetailsDto(savedQuest);
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
            }
        }
    }


    public List<QuestDetailsDto> getAllQuestsForAdmin() {
        // Conversie Set -> List implicită în Stream
        return questRepository.findAllWithCriteria().stream()
                .map(this::mapToDetailsDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public QuestDetailsDto updateQuest(Long questId, QuestCreateDto updateDto) {
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new ResourceNotFoundException("Quest-ul nu a fost găsit."));

        // ... (Setări câmpuri simple: title, description, rewards etc.) ...
        quest.setTitle(updateDto.getTitle());
        // ... restul setărilor ...
        quest.setActive(updateDto.isActive()); // Setăm noua stare

        // 1. GESTIONARE CRITERII (Ștergere Vechi)
        // Mai întâi ștergem progresul VECHI pentru toți utilizatorii (pentru a nu avea orfani)
        // Deoarece vom șterge criteriile, progresul legat de ele trebuie să dispară.
        // Asta se aplică și utilizatorilor ACTIVE și celor EXPIRED.
        if (quest.getCriteria() != null) {
            for (QuestCriterion oldCriterion : quest.getCriteria()) {
                criterionProgressRepository.deleteByCriterion(oldCriterion);
            }
        }
        quest.getCriteria().clear();

        // 2. GESTIONARE CRITERII (Adăugare Noi)
        if (updateDto.getCriteria() != null && !updateDto.getCriteria().isEmpty()) {
            Set<QuestCriterion> newCriteria = updateDto.getCriteria().stream()
                    .map(dto -> {
                        QuestCriterion criterion = mapToEntity(dto, quest);
                        criterion.setQuest(quest);
                        return questCriterionRepository.save(criterion);
                    })
                    .collect(Collectors.toSet());
            quest.setCriteria(newCriteria);
        }

        Quest updatedQuest = questRepository.save(quest);

        // 3. LOGICA DE REACTIVARE UTILIZATORI
        // Dacă Quest-ul este acum ACTIV, căutăm utilizatorii care îl aveau EXPIRED și îl reactivăm.
        if (updatedQuest.isActive()) {
            List<UserQuestLog> expiredLogs = questLogRepository.findByQuestIdAndStatus(questId, QuestStatus.EXPIRED);

            for (UserQuestLog log : expiredLogs) {
                log.setStatus(QuestStatus.ACTIVE);
                log.setCompletionDate(null); // Resetăm data
                questLogRepository.save(log);

                // CRITIC: Re-inițializăm progresul pentru NOILE criterii
                initializeCriterionProgress(log, updatedQuest.getCriteria());
            }

            // Opțional: Trebuie să re-inițializăm progresul și pentru cei care erau ACTIVE?
            // Da, pentru că am șters criteriile vechi!
            List<UserQuestLog> activeLogs = questLogRepository.findByQuestIdAndStatus(questId, QuestStatus.ACTIVE);
            for (UserQuestLog log : activeLogs) {
                // initializeCriterionProgress adaugă doar ce lipsește, deci e sigur
                initializeCriterionProgress(log, updatedQuest.getCriteria());
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

        // 2. Caută toți utilizatorii care au acest quest ACTIV
        List<UserQuestLog> activeUserLogs = questLogRepository.findByQuestIdAndStatus(questId, QuestStatus.ACTIVE);

        // 3. Marchează-le ca EXPIRED
        for (UserQuestLog log : activeUserLogs) {
            log.setStatus(QuestStatus.EXPIRED);
            log.setCompletionDate(LocalDateTime.now()); // Setăm data pentru a apărea corect în istoric
            questLogRepository.save(log);
        }
    }


    // --- User Flow: Asignare și Progres ---

    @Transactional
    public void assignActiveQuests(User user) {
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

    // CORECTAT: Acceptă Collection (Set)
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

    // Metodă ON-DEMAND: Nu mai este apelată automat la purchase, ci doar la verificare
    @Transactional
    public void updateQuestProgress(User user, QuestType eventType, ProductCategory category, Long productId, double amount) {
        // Această metodă a rămas pentru compatibilitate, dar logica principală
        // ar trebui să fie în calculul On-Demand sau în apelurile directe
        // din ShopService (dacă păstrezi logica bazată pe evenimente).

        // DEOARECE AM TRECUT PE ISTORIC (On-Demand), această metodă este mai puțin critică
        // pentru fluxul instant, dar poate fi păstrată pentru actualizări incrementale dacă dorești.

        // ... (logica veche, ajustată la Set) ...
        // Pentru moment, să ne bazăm pe getUserQuestLog care recalculează totul.
    }

    // --- Metodă care Calculează Progresul la Cerere (On-Demand) ---

    // Aceasta este folosită intern de getUserQuestLog pentru afișare
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

            return transactions.stream()
                    .mapToInt(CoinTransaction::getAmount)
                    .sum();

        }

        return 0.0;
    }

    // --- User Flow: claimReward ---
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireOverdueQuests() {
        System.out.println("--- CRON: Checking for expired quests... ---");

        // 1. Preia toate quest-urile active ale utilizatorilor
        List<UserQuestLog> activeLogs = questLogRepository.findAllActiveLogsWithQuest();
        LocalDate today = LocalDate.now();
        int expiredCount = 0;

        for (UserQuestLog log : activeLogs) {
            // 2. Calculează data limită
            // Data limită = Data Start + Durata (zile)
            int duration = log.getQuest().getDurationDays();
            LocalDate expirationDate = log.getStartDate().plusDays(duration);

            // 3. Verifică dacă a trecut timpul
            // Dacă azi este DUPĂ data expirării
            if (today.isAfter(expirationDate)) {
                log.setStatus(QuestStatus.EXPIRED);
                log.setCompletionDate(LocalDateTime.now()); // Setăm data expirării

                questLogRepository.save(log);
                expiredCount++;
            }
        }

        System.out.println("--- CRON: Expired " + expiredCount + " quests. ---");
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

        if (quest.getRewardCoins() != null && quest.getRewardCoins() > 0) {
            int coins = quest.getRewardCoins();
            String description = "Recompensă Quest: " + quest.getTitle();

            user.setCoins(user.getCoins() + coins);

            coinTransactionRepository.save(CoinTransaction.builder()
                    .user(user)
                    .amount(coins)
                    .description(description)
                    .transactionType("QUEST_REWARD")
                    .createdAt(LocalDateTime.now())
                    .build());
            userRepository.save(user);
        }

        if (quest.getRewardXp() != null && quest.getRewardXp() > 0.0) {
            experienceService.addExperience(user, quest.getRewardXp(), "QUEST_REWARD", null, "Recompensă Quest: " + quest.getTitle());
        }

        if (quest.getRewardProduct() != null) {
            Product rewardProduct = quest.getRewardProduct();
            UserInventoryItem newItem = UserInventoryItem.builder()
                    .user(user)
                    .product(rewardProduct)
                    .status("IN_INVENTORY")
                    .build();
            inventoryService.saveInventoryItem(newItem);
        }

        log.setStatus(QuestStatus.REWARDED);
        UserQuestLog savedLog = questLogRepository.save(log);

        return mapToLogDto(savedLog);
    }

    public List<UserQuestLogDto> getUserQuestLog(User user) {
        // Acum Repository-ul returnează și EXPIRED
        List<UserQuestLog> logs = questLogRepository.findAllByUserAndStatusInForDisplay(user);

        return logs.stream()
                // Filtrăm doar să nu fie REWARDED foarte vechi. EXPIRED trece.
                .filter(log -> log.getStatus() != QuestStatus.REWARDED || log.getCompletionDate().isAfter(LocalDateTime.now().minusDays(30)))
                .map(log -> {

                    // Dacă e EXPIRED, nu recalculăm progresul (nu are sens, e istoric)
                    if (log.getStatus() == QuestStatus.EXPIRED) {
                        return mapToLogDto(log);
                    }

                    Set<UserCriterionProgress> currentProgressSet = log.getCriterionProgress();

                    // Dacă cumva setul e gol (din cauza unui update anterior), încercăm să-l populăm
                    // Aceasta este o plasă de siguranță
                    if (currentProgressSet == null || currentProgressSet.isEmpty()) {
                        initializeCriterionProgress(log, log.getQuest().getCriteria());
                        // Reîncărcăm pentru a avea datele
                        currentProgressSet = log.getCriterionProgress();
                    }

                    for (UserCriterionProgress p : currentProgressSet) {
                        if (!p.getIsCompleted()) {
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
                        // NOTIFICARE:
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
    public UserQuestLogDto getQuestPreview(Long questId) {
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new ResourceNotFoundException("Quest-ul nu a fost găsit."));

        UserQuestLog mockLog = UserQuestLog.builder()
                .quest(quest)
                .status(QuestStatus.ACTIVE)
                .startDate(LocalDate.now())
                .build();

        Set<UserCriterionProgress> mockProgressList = quest.getCriteria().stream()
                .map(criterion -> {
                    return UserCriterionProgress.builder()
                            .userQuestLog(mockLog)
                            .criterion(criterion)
                            .currentProgress(0.0)
                            .isCompleted(false)
                            .uniqueKey("MOCK_" + criterion.getId())
                            .build();
                })
                .collect(Collectors.toSet());

        mockLog.setCriterionProgress(mockProgressList);
        return mapToLogDto(mockLog);
    }
}