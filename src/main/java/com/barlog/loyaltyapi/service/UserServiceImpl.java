package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.*;
import com.barlog.loyaltyapi.model.*;
import com.barlog.loyaltyapi.repository.CoinTransactionRepository;
import com.barlog.loyaltyapi.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor; // Importă adnotarea
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // 1. Adaugă adnotarea Lombok
public class UserServiceImpl implements UserService {

    // 2. Marchează TOATE dependențele ca 'final'
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CoinTransactionRepository coinTransactionRepository;
    private final ExperienceService experienceService;
    private final FileStorageService fileStorageService;
 //   private final BonusService bonusService;
 private final BonusService bonusService;
    private final QuestService questService;
    @Override
    public User registerUser(RegisterUserDto registerUserDto) {
        Optional<User> existingUserOptional = userRepository.findByEmail(registerUserDto.getEmail());

        if (existingUserOptional.isPresent()) {
            User existingUser = existingUserOptional.get();
            if (existingUser.getAuthProvider() == AuthProvider.GOOGLE) {
                throw new IllegalStateException("Un cont cu acest email a fost deja creat folosind Google. Vă rugăm să vă autentificați cu Google.");
            } else {
                throw new IllegalStateException("Emailul este deja folosit.");
            }
        }
        // 1. Generare Recovery Key (UUID scurtat)
        String recoveryKey = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        User newUser = User.builder()
                .firstName(registerUserDto.getFirstName())
                .lastName(registerUserDto.getLastName())
                .email(registerUserDto.getEmail())
                .password(passwordEncoder.encode(registerUserDto.getPassword()))
                .role(Role.ROLE_USER)
                .authProvider(AuthProvider.LOCAL)
                .coins(0)
                .xpRate(1.0)
                .experience(0.0)
                .recoveryKey(recoveryKey)
                .build();

        User savedUser = userRepository.save(newUser);
        questService.assignActiveQuests(savedUser);
        return savedUser;
    }


    @Transactional
    public User generateNewRcoveryKey(User currentUser) {
        // 1. Generare nouă cheie
        String newKey = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 2. Aplică cheia și resetează 'lastUsed'
        currentUser.setRecoveryKey(newKey);
        currentUser.setRecoveryKeyLastUsed(null); // Marchează ca disponibilă pentru utilizare

        return userRepository.save(currentUser);
    }
    @Override // 3. Adaugă @Override pentru a asigura implementarea corectă a interfeței
    @Transactional
    public User claimReceiptCoins(User currentUser, ClaimRequestDto claimRequest) {
        if (claimRequest.getAmount() == null || claimRequest.getAmount() <= 0) {
            throw new IllegalArgumentException("Suma de revendicat trebuie să fie pozitivă.");
        }

 //       double coinMultiplier = bonusService.calculateMultiplier(currentUser, ItemEffectType.COIN_BOOST_GLOBAL, null);

        // 2. Aplicăm bonusul
      //  int baseAmount = claimRequest.getAmount();
   //     int finalAmount = (int) Math.round(baseAmount * coinMultiplier);

  //      currentUser.setCoins(currentUser.getCoins() + finalAmount);

        CoinTransaction transaction = CoinTransaction.builder()
                .user(currentUser)
                .amount(claimRequest.getAmount())
                .description(claimRequest.getDescription())
                .transactionType("RECEIPT_CLAIM")
                .createdAt(LocalDateTime.now())
                .build();

        coinTransactionRepository.save(transaction);
        experienceService.addExperienceForReceiptClaim(currentUser, claimRequest.getAmount());
        return userRepository.save(currentUser);
    }
    @Transactional
    public User updateNickname(User currentUser, String newNickname) {
        if (userRepository.findByNickname(newNickname).isPresent()) {
            throw new IllegalStateException("Acest nickname este deja folosit.");
        }
        currentUser.setNickname(newNickname);
        return userRepository.save(currentUser);
    }
    @Override
    @Transactional
    public User updateAvatar(User currentUser, MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Fișierul nu poate fi gol.");
        }

        // 1. Șterge avatarul vechi (dacă există)
        if (currentUser.getAvatarUrl() != null && !currentUser.getAvatarUrl().isEmpty()) {
            fileStorageService.deleteFile(currentUser.getAvatarUrl());
        }

        // 2. Încarcă fișierul nou și obține Public ID-ul
        String newPublicId = fileStorageService.storeFile(file);

        // 3. Salvează noul ID-ul în baza de date
        currentUser.setAvatarUrl(newPublicId);

        return userRepository.save(currentUser);
    }

    @Override
    @Transactional
    public User generateNewRecoveryKey(User currentUser) {
        // 1. Generare nouă cheie (UUID scurtat la 8 caractere)
        String newKey = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 2. Aplică cheia și resetează 'lastUsed' pentru a o face validă
        currentUser.setRecoveryKey(newKey);
        currentUser.setRecoveryKeyLastUsed(null); // Marchează ca disponibilă pentru utilizare

        return userRepository.save(currentUser);
    }

    @Override
    public List<AllUsersDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToAllUsersDto)
                .collect(Collectors.toList());
    }
    protected AllUsersDTO mapToAllUsersDto(User user) {
        AllUsersDTO dto = new AllUsersDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setNickname(user.getNickname());
        return dto;
    }




   @Override
    @Transactional
    public void distributePoints(User user, PointDistributionDto req) {
        // 1. Verificăm dacă sunt valori negative (nu poți scădea stat-uri ca să primești puncte înapoi)
        if (req.getStr() < 0 || req.getDex() < 0 || req.getIntel() < 0 || req.getCha() < 0) {
            throw new IllegalArgumentException("Nu poți folosi valori negative.");
        }

        // 2. Calculăm totalul cerut
        int totalRequested = req.getStr() + req.getDex() + req.getIntel() + req.getCha();

        // 3. Verificăm dacă userul are destule puncte
        if (totalRequested > user.getUnallocatedPoints()) {
            throw new IllegalStateException("Nu ai suficiente puncte disponibile! Ai doar " + user.getUnallocatedPoints());
        }

        if (totalRequested == 0) return; // Nimic de făcut

        // 4. Aplicăm modificările
        user.setStrength(user.getStrength() + req.getStr());
        user.setDexterity(user.getDexterity() + req.getDex());
        user.setIntelligence(user.getIntelligence() + req.getIntel());
        user.setCharisma(user.getCharisma() + req.getCha());

        // 5. Scădem din portofelul de puncte
        user.setUnallocatedPoints(user.getUnallocatedPoints() - totalRequested);

        userRepository.save(user);
    }

}