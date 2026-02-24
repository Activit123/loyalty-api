package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.*;
import com.barlog.loyaltyapi.exception.ResourceNotFoundException;
import com.barlog.loyaltyapi.model.*;
import com.barlog.loyaltyapi.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final CoinTransactionRepository coinTransactionRepository;
    private final LevelService levelService;
    private final ExperienceService experienceService;
    private final CharacterService characterService;
    private final FileStorageService fileStorageService; // ASIGURĂ-TE CĂ ACEASTA E INJECTATĂ
    private final UserNotificationService notificationService; // INJECTAT
    private final Random random = new Random();
    private final BonusService bonusService;
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final UserInventoryItemRepository userInventoryItemRepository;
    private final ItemTemplateRepository itemTemplateRepository;
    private final UserItemRepository userItemRepository;


    @Transactional
    public void giftItemOrProduct(AdminGiftRequest request) {
        User user = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Utilizator negăsit: " + request.getUserEmail()));

        if ("ITEM".equalsIgnoreCase(request.getType())) {
            // Gift RPG Item
            ItemTemplate item = itemTemplateRepository.findById(request.getTargetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item RPG negăsit ID: " + request.getTargetId()));

            UserItem newItem = UserItem.builder()
                    .user(user)
                    .itemTemplate(item)
                    .isEquipped(false)
                    .build();
            userItemRepository.save(newItem);

            notificationService.notifyUser(user, "Adminul ți-a oferit un cadou: " + item.getName(), NotificationType.SYSTEM, "/character");

        } else if ("PRODUCT".equalsIgnoreCase(request.getType())) {
            // Gift Produs Fizic
            Product product = productRepository.findById(request.getTargetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produs negăsit ID: " + request.getTargetId()));

            UserInventoryItem newInvItem = UserInventoryItem.builder()
                    .user(user)
                    .product(product)
                    .status("IN_INVENTORY")
                    .build();
            userInventoryItemRepository.save(newInvItem);

            notificationService.notifyUser(user, "Adminul ți-a oferit un produs: " + product.getName(), NotificationType.SYSTEM, "/inventory");
        } else {
            throw new IllegalArgumentException("Tip invalid. Folosește 'ITEM' sau 'PRODUCT'.");
        }
    }


    public UserResponseDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        return mapUserToDto(user);
    }
    // --- Metodă Nouă ---
    @Transactional
    public UserResponseDto addCoinsToUserByEmail(String email, AddCoinsRequestDto request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
        experienceService.addExperienceForReceiptClaim(user,request.amount());


        return processCoinAddition(user, request.amount(), request.description());
}
    // --- Metodă Nouă ---
    public UserResponseDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
        return mapUserToDto(user);
    }
    // --- METODA PRIVATĂ ACTUALIZATĂ PENTRU LUCKY SCAN ---
    private UserResponseDto processCoinAddition(User user, Integer baseAmount, String description) {
        int finalAmount = baseAmount;
        String finalDescription = description;

        // Doar dacă adăugăm monede (nu dacă scădem)
        if (baseAmount > 0) {

            // 1. Verificăm multiplicatorul global de monede (COIN_BOOST_GLOBAL)
            // Dacă nu are niciun bonus, va returna 1.0
            double coinMultiplier = bonusService.calculateMultiplier(user, ItemEffectType.COIN_BOOST_GLOBAL, null);

            if (coinMultiplier > 1.0) {
                finalAmount = (int) Math.round(baseAmount * coinMultiplier);
                finalDescription += String.format(" (Bonus Echipament: %.0f%%)", (coinMultiplier - 1.0) * 100);
            }

            // 2. Verificăm șansa de LUCKY_SCAN (Monedă bonus la noroc)
            // Suma valorilor efectelor LUCKY_SCAN_BONUS (ex: inel cu 5.0 + amuletă cu 10.0 = 15.0 șansă)
            double luckChance = bonusService.calculateFlatBonus(user, ItemEffectType.LUCKY_SCAN_BONUS);

            // Generăm un număr între 0.0 și 100.0
            double roll = random.nextDouble() * 100.0;

            // Dacă roll-ul este sub șansă (ex: roll 12 < 15), userul a avut noroc
            if (roll < luckChance) {
                finalAmount += 1; // Adăugăm moneda extra
                finalDescription += " (+1 Noroc)"; // Marcăm în istoric

                // Trimitem o notificare specială pentru noroc
                notificationService.notifyUser(
                        user,
                        "Noroc Chior! Ai primit o monedă în plus de la echipamentul tău!",
                        NotificationType.SYSTEM,
                        "/profile"
                );
            }
        }

        user.setCoins(user.getCoins() + finalAmount);

        CoinTransaction transaction = CoinTransaction.builder()
                .user(user)
                .amount(finalAmount)
                .description(finalDescription)
                .transactionType(finalAmount > 0 ? "ADMIN_ADD" : "ADMIN_REMOVE")
                .createdAt(LocalDateTime.now())
                .build();
        coinTransactionRepository.save(transaction);

        if (finalAmount > 0) {
            notificationService.notifyUser(
                    user,
                    "Ai primit " + finalAmount + " Monede! (" + finalDescription + ")",
                    NotificationType.SYSTEM,
                    "/profile"
            );
        }

        User updatedUser = userRepository.save(user);
        return mapUserToDto(updatedUser);
    }
    @Transactional
    public UserResponseDto addCoinsToUser(Long userId, AddCoinsRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        user.setCoins(user.getCoins() + request.amount());

        CoinTransaction transaction = CoinTransaction.builder()
                .user(user)
                .amount(request.amount())
                .description(request.description())
                .transactionType(request.amount() > 0 ? "ADMIN_ADD" : "ADMIN_REMOVE")
                .createdAt(LocalDateTime.now())
                .build();
        coinTransactionRepository.save(transaction);

        User updatedUser = userRepository.save(user);
        return mapUserToDto(updatedUser);
    }
    @Transactional
    public void addManualExperienceToUserByEmail(String email, int amount) {
        // Găsim utilizatorul după email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizatorul cu emailul " + email + " nu a fost găsit."));

        // Delegăm logica specifică de business (crearea tranzacției, actualizarea totalului)
        // către serviciul care știe cel mai bine să facă asta.
        experienceService.addManualExperience(user.getEmail(), amount);
    }
    @Transactional
    public UserResponseDto adjustUserCoins(Long userId, AdjustCoinsRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilizatorul cu ID-ul " + userId + " nu a fost găsit."));

        user.setCoins(request.getNewCoinBalance());

        User updatedUser = userRepository.save(user);
        return mapUserToDto(updatedUser);
    }

    /**
     * Returnează top 10 utilizatori pe baza punctelor de loialitate (coins).
     */
    public List<UserLeaderboardDto> getLeaderboard() {
        // CORECTAT: Folosește metoda corectă de sortare
        List<User> topUsers = userRepository.findTop10ByOrderByExperienceDesc();

        return topUsers.stream()
                .map(user -> {

                    // NOU: Logica de generare Avatar URL
                    String avatarUrl = null;
                    if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                        avatarUrl = fileStorageService.getImageUrlFromPublicId(user.getAvatarUrl());
                    }
                    // Verificăm dacă are abonament în ultimele 30 de zile
                    LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
                    boolean isPrestige = coinTransactionRepository.hasActiveSubscription(user, thirtyDaysAgo);

                    return new UserLeaderboardDto(
                            user.getFirstName(),
                            user.getLastName(),
                            user.getEmail(),
                            user.getCoins(),
                            user.getExperience(), // Tipul este Double
                            user.getNickname(),
                            isPrestige,
                            avatarUrl, // NOU: Avatar URL
                            levelService.calculateLevelInfo(user.getExperience())
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Returnează istoricul global al tranzacțiilor.
     */
    public List<TransactionDetailsDto> getGlobalTransactions() {
        List<CoinTransaction> transactions = coinTransactionRepository.findAllByOrderByCreatedAtDesc();
        return transactions.stream()
                .map(transaction -> new TransactionDetailsDto(
                        transaction.getId(),
                        transaction.getUser().getEmail(),
                        transaction.getAmount(),
                        transaction.getCreatedAt(),
                        transaction.getDescription()
                ))
                .collect(Collectors.toList());
    }

    public UserResponseDto mapUserToDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setCoins(user.getCoins());
        dto.setRole(user.getRole());
        dto.setXpRate(user.getXpRate());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setNickname(user.getNickname());
        dto.setExperience(user.getExperience());
        dto.setConsecutiveActivityDays(user.getConsecutiveActivityDays());
        dto.setRace(user.getRace() != null ? characterService.mapToRaceDto(user.getRace()) : null);
        dto.setClassType(user.getClassType() != null ? characterService.mapToClassTypeDto(user.getClassType()) : null);
        dto.setLevelInfo(levelService.calculateLevelInfo(user.getExperience()));
        dto.setStrength(user.getStrength());
        dto.setDexterity(user.getDexterity());
        dto.setIntelligence(user.getIntelligence());
        dto.setCharisma(user.getCharisma());
        dto.setUnallocatedPoints(user.getUnallocatedPoints());
        // Verificăm dacă are abonament în ultimele 30 de zile
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        boolean isPrestige = coinTransactionRepository.hasActiveSubscription(user, thirtyDaysAgo);
        dto.setHasPrestige(isPrestige);
        // NOU: Maparea Avatarului
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            dto.setAvatarUrl(fileStorageService.getImageUrlFromPublicId(user.getAvatarUrl()));
        } else {
            dto.setAvatarUrl(null); // Sau URL-ul unui avatar implicit
        }

        dto.setRecoveryKey(user.getRecoveryKey());
        return dto;
    }

    @Transactional
    public void recalculateStatsForEveryone() {
        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            // 1. Reset la baza rasei (sau 1 dacă nu are rasă)
            if (user.getRace() != null) {
                user.setStrength(user.getRace().getBaseStr());
                user.setDexterity(user.getRace().getBaseDex());
                user.setIntelligence(user.getRace().getBaseInt());
                user.setCharisma(user.getRace().getBaseCha());
            } else {
                user.setStrength(1);
                user.setDexterity(1);
                user.setIntelligence(1);
                user.setCharisma(1);
            }

            // 2. Calcul puncte de nivel
            // Formula: (Level - 1) * 5
            LevelInfoDto levelInfo = levelService.calculateLevelInfo(user.getExperience());
            int pointsEarned = (levelInfo.getLevel() - 1) * 5;

            user.setUnallocatedPoints(pointsEarned);

            userRepository.save(user);
        }
    }
}
