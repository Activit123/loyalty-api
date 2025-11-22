package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.ClaimRequestDto;
import com.barlog.loyaltyapi.dto.RegisterUserDto;
import com.barlog.loyaltyapi.model.AuthProvider;
import com.barlog.loyaltyapi.model.CoinTransaction;
import com.barlog.loyaltyapi.model.Role;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.repository.CoinTransactionRepository;
import com.barlog.loyaltyapi.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor; // Importă adnotarea
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor // 1. Adaugă adnotarea Lombok
public class UserServiceImpl implements UserService {

    // 2. Marchează TOATE dependențele ca 'final'
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CoinTransactionRepository coinTransactionRepository;
    private final ExperienceService experienceService;
    private final FileStorageService fileStorageService;

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
                .build();

        return userRepository.save(newUser);
    }



    @Override // 3. Adaugă @Override pentru a asigura implementarea corectă a interfeței
    @Transactional
    public User claimReceiptCoins(User currentUser, ClaimRequestDto claimRequest) {
        if (claimRequest.getAmount() == null || claimRequest.getAmount() <= 0) {
            throw new IllegalArgumentException("Suma de revendicat trebuie să fie pozitivă.");
        }

        currentUser.setCoins(currentUser.getCoins() + claimRequest.getAmount());

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
}