package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.AddCoinsRequestDto;
import com.barlog.loyaltyapi.dto.UserResponseDto;
import com.barlog.loyaltyapi.model.CoinTransaction;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.repository.CoinTransactionRepository;
import com.barlog.loyaltyapi.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final CoinTransactionRepository coinTransactionRepository;

    public UserResponseDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        return mapUserToDto(user);
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

    public UserResponseDto mapUserToDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setCoins(user.getCoins());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}