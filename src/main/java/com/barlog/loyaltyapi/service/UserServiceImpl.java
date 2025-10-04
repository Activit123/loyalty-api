package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.RegisterUserDto;
import com.barlog.loyaltyapi.model.Role;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User registerUser(RegisterUserDto registerUserDto) {
        // 1. Verificăm dacă există deja un utilizator cu acest email
        if (userRepository.findByEmail(registerUserDto.getEmail()).isPresent()) {
            // Aruncăm o excepție dacă emailul este deja folosit
            throw new IllegalStateException("Email already in use");
        }

        // 2. Construim un obiect User nou
        User user = User.builder()
                .firstName(registerUserDto.getFirstName())
                .lastName(registerUserDto.getLastName())
                .email(registerUserDto.getEmail())
                // 3. Criptăm parola înainte de a o salva
                .password(passwordEncoder.encode(registerUserDto.getPassword()))
                // 4. Setăm rolul implicit pentru orice utilizator nou
                .role(Role.ROLE_USER)
                .silverCoins(0) // Setăm valoarea inițială
                .hasGoldSubscription(false) // Setăm valoarea inițială
                .build();

        // 5. Salvăm utilizatorul în baza de date și îl returnăm
        return userRepository.save(user);
    }
}