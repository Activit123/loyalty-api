package com.barlog.loyaltyapi.config;

import com.barlog.loyaltyapi.security.JwtAuthenticationFilter;
import com.barlog.loyaltyapi.security.OAuth2AuthSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    // Injectăm AuthenticationProvider definit în ApplicationConfig
    private final AuthenticationProvider authenticationProvider;
    private final OAuth2AuthSuccessHandler oAuth2AuthSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**", "/api/auth/register","/api/**")
                ).oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2AuthSuccessHandler)
        )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/register", // Doar /register este public
                                "/api/auth/login",
                                "/h2-console/**",
                                "/api/admin-setup/create-admin",
                                "api/users/me",
                                "/oauth2/**",
                                "/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider) // Folosim provider-ul injectat
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}