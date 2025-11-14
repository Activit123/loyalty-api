package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.ClaimRequestDto;
import com.barlog.loyaltyapi.dto.RegisterUserDto;
import com.barlog.loyaltyapi.model.*;
import com.barlog.loyaltyapi.repository.CoinTransactionRepository;
import com.barlog.loyaltyapi.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CoinTransactionRepository coinTransactionRepository;
    private final ExperienceService experienceService;

    @Override
    @Transactional
    public User registerUser(RegisterUserDto registerUserDto) {
        Optional<User> existingUserOptional = userRepository.findByEmail(registerUserDto.getEmail());
        if (existingUserOptional.isPresent()) {
            throw new IllegalStateException("Emailul este deja folosit.");
        }

        User newUser = User.builder()
                .firstName(registerUserDto.getFirstName())
                .lastName(registerUserDto.getLastName())
                .email(registerUserDto.getEmail())
                .password(passwordEncoder.encode(registerUserDto.getPassword()))
                .role(Role.ROLE_USER)
                .authProvider(AuthProvider.LOCAL)
                .build();

        initializeLoginBonus(newUser);

        return userRepository.save(newUser);
    }

    @Override
    @Transactional
    public void updateConsecutiveLoginBonus(User user) {
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();

        if (dayOfWeek == DayOfWeek.MONDAY || dayOfWeek == DayOfWeek.TUESDAY) {
            return;
        }

        LocalDate lastLogin = user.getLastLoginDate();
        if (lastLogin != null && lastLogin.isEqual(today)) {
            return;
        }

        int consecutiveDays = user.getConsecutiveLoginDays() != null ? user.getConsecutiveLoginDays() : 0;

        boolean isConsecutiveAfterWeekend = (lastLogin != null && lastLogin.getDayOfWeek() == DayOfWeek.SUNDAY && today.getDayOfWeek() == DayOfWeek.WEDNESDAY && today.minusDays(3).isEqual(lastLogin));

        if ((lastLogin != null && lastLogin.isEqual(today.minusDays(1))) || isConsecutiveAfterWeekend) {
            user.setConsecutiveLoginDays(consecutiveDays + 1);
        } else {
            user.setConsecutiveLoginDays(1);
        }

        if (user.getConsecutiveLoginDays() >= 8) {
            user.setXpRate(4.0);
        } else if (user.getConsecutiveLoginDays() >= 4) {
            user.setXpRate(2.0);
        } else {
            user.setXpRate(1.0);
        }

        user.setLastLoginDate(today);
        userRepository.save(user);
    }

    private void initializeLoginBonus(User user) {
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();

        if (dayOfWeek != DayOfWeek.MONDAY && dayOfWeek != DayOfWeek.TUESDAY) {
            user.setConsecutiveLoginDays(1);
            user.setLastLoginDate(today);
            user.setXpRate(1.0);
        } else {
            user.setConsecutiveLoginDays(0);
            user.setXpRate(1.0);
            user.setLastLoginDate(null);
        }
    }

    @Override
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

    @Override
    @Transactional
    public User updateNickname(User currentUser, String newNickname) {
        if (userRepository.findByNickname(newNickname).isPresent()) {
            throw new IllegalStateException("Acest nickname este deja folosit.");
        }
        currentUser.setNickname(newNickname);
        return userRepository.save(currentUser);
    }
}