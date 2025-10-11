package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.RegisterUserDto;
import com.barlog.loyaltyapi.model.AuthProvider;
import com.barlog.loyaltyapi.model.Role;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

        // Creăm noul utilizator și setăm explicit TOATE câmpurile necesare
        User newUser = User.builder()
                .firstName(registerUserDto.getFirstName())
                .lastName(registerUserDto.getLastName())
                .email(registerUserDto.getEmail())
                .password(passwordEncoder.encode(registerUserDto.getPassword()))
                .role(Role.ROLE_USER)
                .authProvider(AuthProvider.LOCAL)
                .coins(0)               // <-- Asigurăm setarea valorii implicite// <-- Asigurăm setarea valorii implicite
                .build();

        return userRepository.save(newUser);
    }
}