package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.QuestCreateDto;
import com.barlog.loyaltyapi.dto.QuestCriterionDto;
import com.barlog.loyaltyapi.dto.QuestDetailsDto;
import com.barlog.loyaltyapi.dto.UserCriterionProgressDto;
import com.barlog.loyaltyapi.dto.UserQuestLogDto;
import com.barlog.loyaltyapi.exception.ResourceNotFoundException;
import com.barlog.loyaltyapi.model.CoinTransaction;
import com.barlog.loyaltyapi.model.Product;
import com.barlog.loyaltyapi.model.ProductCategory;
import com.barlog.loyaltyapi.model.Quest;
import com.barlog.loyaltyapi.model.QuestCriterion;
import com.barlog.loyaltyapi.model.QuestStatus;
import com.barlog.loyaltyapi.model.QuestType;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.model.UserCriterionProgress;
import com.barlog.loyaltyapi.model.UserInventoryItem;
import com.barlog.loyaltyapi.model.UserQuestLog;
import com.barlog.loyaltyapi.repository.CoinTransactionRepository;
import com.barlog.loyaltyapi.repository.QuestRepository;
import com.barlog.loyaltyapi.repository.UserCriterionProgressRepository;
import com.barlog.loyaltyapi.repository.UserQuestLogRepository;
import com.barlog.loyaltyapi.repository.ProductRepository;
import com.barlog.loyaltyapi.repository.UserRepository;
import com.barlog.loyaltyapi.repository.QuestCriterionRepository;
import lombok.RequiredArgsConstructor;
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

        Product rewardProduct = updateDto.getRewardProductId() != null
                ? productRepository.findById(updateDto.getRewardProductId()).orElse(null)
                : null;

        // Actualizarea câmpurilor de bază
        quest.setTitle(updateDto.getTitle());
        quest.setDescription(updateDto.getDescription());
        quest.setDurationDays(updateDto.getDurationDays());
        quest.setType(QuestType.valueOf(updateDto.getType()));
        quest.setRewardCoins(updateDto.getRewardCoins());
        quest.setRewardXp(updateDto.getRewardXp());
        quest.setRewardProduct(rewardProduct);
        quest.setActive(updateDto.isActive());

        // 4. ACTUALIZAREA CRITERIILOR

        // PAS CRITIC NOU: Șterge progresul utilizatorilor legat de criteriile vechi
        // Înainte de a șterge criteriile, trebuie să rupem legătura din user_criterion_progress
        if (quest.getCriteria() != null && !quest.getCriteria().isEmpty()) {
            for (QuestCriterion oldCriterion : quest.getCriteria()) {
                // Ștergem tot progresul legat de acest criteriu pentru a evita eroarea de Foreign Key
                criterionProgressRepository.deleteByCriterion(oldCriterion);
            }
        }

        // Pasul A: Acum putem goli colecția în siguranță
        quest.getCriteria().clear();

        // Pasul B: Construim și adăugăm noile criterii
        if (updateDto.getCriteria() != null && !updateDto.getCriteria().isEmpty()) {
            Set<QuestCriterion> newCriteria = updateDto.getCriteria().stream()
                    .map(dto -> {
                        QuestCriterion criterion = mapToEntity(dto, quest);
                        criterion.setQuest(quest);
                        return criterion;
                    })
                    .collect(Collectors.toSet());

            quest.getCriteria().addAll(newCriteria);
        }

        // 5. Salvarea Quest-ului
        Quest updatedQuest = questRepository.save(quest);

        return mapToDetailsDto(updatedQuest);
    }
    @Transactional
    public void deleteQuest(Long questId) {
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new ResourceNotFoundException("Quest-ul cu ID-ul " + questId + " nu a fost găsit."));
        quest.setActive(false);
        questRepository.save(quest);
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
        List<UserQuestLog> logs = questLogRepository.findAllByUserAndStatusInForDisplay(user);

        return logs.stream()
                .filter(log -> log.getStatus() != QuestStatus.REWARDED || log.getCompletionDate().isAfter(LocalDateTime.now().minusDays(30)))
                .map(log -> {

                    // 1. Recalculăm progresul (doar în memorie sau salvăm individual)
                    // NU înlocuim colecția de pe 'log' cu setCriterionProgress(newSet)

                    // Preluăm progresul curent din DB (pentru a evita LazyInit dacă nu a fost fetch-uit)
                    // Dar cum am scos fetch-ul, accesarea log.getCriterionProgress() aici va face un SELECT.
                    Set<UserCriterionProgress> currentProgressSet = log.getCriterionProgress();

                    // Actualizăm valorile obiectelor existente în set
                    for (UserCriterionProgress p : currentProgressSet) {
                        if (!p.getIsCompleted()) {
                            double historyProgress = calculateProgressFromHistory(user, p.getCriterion(), log.getStartDate().atStartOfDay());

                            if (historyProgress > p.getCurrentProgress()) {
                                p.setCurrentProgress(Math.min(historyProgress, p.getCriterion().getRequiredAmount()));
                                p.setIsCompleted(historyProgress >= p.getCriterion().getRequiredAmount());
                                criterionProgressRepository.save(p); // Salvăm actualizarea
                            }
                        }
                    }

                    // Verificăm completarea
                    boolean allMet = currentProgressSet.stream().allMatch(UserCriterionProgress::getIsCompleted);
                    if (allMet && log.getStatus() == QuestStatus.ACTIVE) {
                        log.setStatus(QuestStatus.COMPLETED);
                        log.setCompletionDate(LocalDateTime.now());
                        questLogRepository.save(log);
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