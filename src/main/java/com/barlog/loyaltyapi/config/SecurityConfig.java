package com.barlog.loyaltyapi.config;

import com.barlog.loyaltyapi.security.JwtAuthenticationFilter;
import com.barlog.loyaltyapi.security.OAuth2AuthSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                // Activăm CORS folosind bean-ul de mai jos
                .cors(withDefaults())
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**", "/api/**")
                )
                .authorizeHttpRequests(auth -> auth
                        // --- Rute Publice (o listă completă și corectă) ---
                        .requestMatchers(
                                "/api/auth/**",
                                "/oauth2/**",
                                "/api/products", // Listarea de produse
                                "/uploads/images/**", // Imaginile produselor
                                "/api/admin-setup/create-admin",
                                "/h2-console/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api-docs/**"
                        ).permitAll()

                        // --- Rute Specifice pentru Admin ---
                        // Orice rută care începe cu /api/admin/ necesită rolul ADMIN
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/products/**").hasRole("ADMIN")
                        // Orice altă rută sub /api/admin/ necesită rolul ADMIN

                        // --- Orice Altă Rută ---
                        // Orice altceva (ex: /api/users/me) necesită doar autentificare
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2AuthSuccessHandler)
                )
                .exceptionHandling(e -> e
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
                "http://172.29.128.1:5173"
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