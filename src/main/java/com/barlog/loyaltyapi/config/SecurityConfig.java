package com.barlog.loyaltyapi.config;

import com.barlog.loyaltyapi.security.JwtAuthenticationFilter;
import com.barlog.loyaltyapi.security.OAuth2AuthSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final OAuth2AuthSuccessHandler oAuth2AuthSuccessHandler;

    // Definim un array static pentru rutele publice pentru o mai bună lizibilitate și mentenanță
    private static final String[] PUBLIC_URLS = {
            // --- Căile corecte și complete pentru Swagger/SpringDoc ---
            "/swagger-ui.html",
            "/swagger-ui/**",          // <-- MODIFICARE CHEIE: Permite accesul la TOATE resursele UI-ului
            "/v3/api-docs/**",         // <-- MODIFICARE CHEIE: Permite accesul la definiția API și resursele conexe
            "/swagger-resources/**",   // <-- Adăugat pentru compatibilitate
            "/webjars/**",             // <-- Adăugat pentru resursele statice (CSS, JS)

            // --- Restul rutelor publice ---
            "/api/auth/**",            // <-- Folosim wildcard pentru a acoperi toate sub-rutele
            "/oauth2/**",
            "/api/products",           // Presupunând că listarea produselor e publică
            "/uploads/images/**",      // <-- Folosim wildcard
            "/api/admin-setup/create-admin",
            "/h2-console/**"           // <-- Folosim wildcard
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(PUBLIC_URLS).permitAll() // Utilizăm array-ul definit mai sus
                        .requestMatchers(HttpMethod.GET, "/api/tables").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/tables").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler(oAuth2AuthSuccessHandler)
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                // MODIFICARE ADĂUGATĂ: Necesar pentru a afișa consola H2 într-un iframe
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "https://localhost:5173",
                "http://ec2-13-53-91-89.eu-north-1.compute.amazonaws.com:5173",
                "https://barlog.netlify.app",
                "http://192.168.1.100:5173",
                "http://172.29.128.1:5173"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // MODIFICARE CHEIE: Înregistrarea pe "/**" este mai robustă și aplică politica pe toate căile
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}