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

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor // 1. Adaugă adnotarea Lombok
public class UserServiceImpl implements UserService {

    // 2. Marchează TOATE dependențele ca 'final'
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CoinTransactionRepository coinTransactionRepository;

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

        return userRepository.save(currentUser);
    }
}