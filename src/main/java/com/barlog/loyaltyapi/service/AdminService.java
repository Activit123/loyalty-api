package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.AddCoinsRequestDto;
import com.barlog.loyaltyapi.dto.AdjustCoinsRequestDto;
import com.barlog.loyaltyapi.dto.TransactionDetailsDto;
import com.barlog.loyaltyapi.dto.UserLeaderboardDto;
import com.barlog.loyaltyapi.dto.UserResponseDto;
import com.barlog.loyaltyapi.model.CoinTransaction;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.repository.CoinTransactionRepository;
import com.barlog.loyaltyapi.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
        return processCoinAddition(user, request);
    }
    // --- Metodă Nouă ---
    public UserResponseDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
        return mapUserToDto(user);
    }
    private UserResponseDto processCoinAddition(User user, AddCoinsRequestDto request) {
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

                    return new UserLeaderboardDto(
                            user.getFirstName(),
                            user.getLastName(),
                            user.getEmail(),
                            user.getCoins(),
                            user.getExperience(), // Tipul este Double
                            user.getNickname(),
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
        // NOU: Maparea Avatarului
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            dto.setAvatarUrl(fileStorageService.getImageUrlFromPublicId(user.getAvatarUrl()));
        } else {
            dto.setAvatarUrl(null); // Sau URL-ul unui avatar implicit
        }
        return dto;
    }
}
