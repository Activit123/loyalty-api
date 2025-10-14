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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**", "/api/**")
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS).permitAll()
                        // --- Rute Publice ---
                        .requestMatchers(
                                "/api/auth/**",
                                "/oauth2/**",
                                // Swagger-ul trebuie să fie complet public
                                // Am actualizat căile pentru a se potrivi cu cele implicite ale SpringDoc
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/api/ai/**",
                                "/v3/api-docs/**", // <-- Calea nouă și corectă
                                // Restul rutelor publice
                                "/api/products",
                                "/uploads/images/**",
                                "/api/admin-setup/create-admin",
                                "/h2-console/**"
                        ).permitAll()

                        // --- Rute Specifice pentru Admin ---
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")

                        // --- Orice Altă Rută ---
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // --- AICI ESTE MODIFICAREA CHEIE ---
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login") // Chiar dacă nu avem o pagină aici, ajută la configurare
                        .successHandler(oAuth2AuthSuccessHandler)
                )
                .exceptionHandling(e -> e
                        // Acest handler este crucial pentru API-urile REST
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Am păstrat lista ta completă de origini permise
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "https://localhost:5173", // Adăugat pentru HTTPS local
                "http://ec2-13-53-91-89.eu-north-1.compute.amazonaws.com:5173",
                "https://barlog.netlify.app",
                "http://192.168.1.100:5173",
                "https://172.29.128.1:5173",
                "https://*:5173",
                "https://bebf35af8d0c.ngrok-free.app",
                "https://*.ngrok-free.app",
                "https://10.11.4.35:5173",
                "http://10.11.4.35:5173",
                "http://172.29.128.1:5173",
                "https://10.48.93.14:5173",
                "http://10.48.93.87:5173"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplicăm configurația pe toate căile, așa cum ai specificat
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}