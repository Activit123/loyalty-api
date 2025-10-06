package com.barlog.loyaltyapi.security;

import com.barlog.loyaltyapi.model.AuthProvider;
import com.barlog.loyaltyapi.model.Role;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.repository.UserRepository;
import com.barlog.loyaltyapi.security.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2AuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException, IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        String firstName = oauth2User.getAttribute("given_name");
        String lastName = oauth2User.getAttribute("family_name");

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            // Utilizatorul există deja, îl actualizăm dacă e cazul
            user = userOptional.get();
            if (user.getAuthProvider() != AuthProvider.GOOGLE) {
                // Poate fi un cont local, îl "legăm" de Google
                user.setAuthProvider(AuthProvider.GOOGLE);
                user.setFirstName(firstName); // Actualizăm cu datele de la Google
                user.setLastName(lastName);
                userRepository.save(user);
            }
        } else {
            // Utilizator nou, creăm un cont nou
            user = User.builder()
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .role(Role.ROLE_USER)
                    .authProvider(AuthProvider.GOOGLE) // Setăm provider-ul corect
                    .build();
            userRepository.save(user);
        }

        // Generăm token-ul JWT pentru utilizatorul nostru
        String jwtToken = jwtService.generateToken(user);

        // Construim URL-ul de redirect către frontend, cu token-ul ca parametru
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/auth/callback")
                .queryParam("token", jwtToken)
                .build().toUriString();

        // Facem redirect-ul
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}