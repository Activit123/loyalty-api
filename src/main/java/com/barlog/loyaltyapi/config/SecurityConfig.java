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

    // Array static pentru rutele publice (folosit pentru lizibilitate și mentenanță)
    private static final String[] PUBLIC_URLS = {
            // Swagger / OpenAPI / SpringDoc
            "/api/app-version/latest",
            "/barlog-app.apk", // Permitem și descărcarea fișierului static
            "/api/admin/quests/sync-users",
            "/api/items/template/**", // <--- ADAUGĂ ACEASTA LINIE
            "/api/users/getAllUsers",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/api/character/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/api/announcements/**",
            // API publice
            "/api/auth/**",
            "/api/notifications/**",
            "api/notifications",
            "/oauth2/**",
            "/api/ai/**",
            "/api/trade/**",
            "api/quests/log",
            "/api/products",
            "/api/games",
            "/api/admin/reporting/leaderboard",
            "/uploads/images/**",
            "/api/admin-setup/create-admin",
            "/api/menu-items",
            // H2 console
            "/h2-console/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CORS
                .cors(withDefaults())

                // CSRF: păstrăm excepțiile utile pentru H2 și API-uri (nu dezactivăm complet)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**", "/api/**")

                )

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tables").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/tables").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                // Stateless sessions pentru API
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Provider și filtre
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // OAuth2 login (dacă este folosit)
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler(oAuth2AuthSuccessHandler)
                )

                // Error handling pentru API REST
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )

                // Permite afișarea consola H2 într-un iframe din aceeași origine
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        // Folosim pattern-uri pentru a permite și wildcard-uri utile (ex: ngrok)
       /* configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "https://localhost:5173",
                "http://ec2-13-53-91-89.eu-north-1.compute.amazonaws.com:5173",
                "https://barlog.netlify.app",
                "http://192.168.1.100:5173",
                "http://172.29.128.1:5173",
                "https://*.ngrok-free.app",
                "https://bebf35af8d0c.ngrok-free.app",
                "http://10.11.4.35:5173",
                "https://10.11.4.35:5173",
                "http://10.48.93.87:5173",
                "https://10.48.93.14:5173"
        ));
*/
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT","PATCH","DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplicăm politica CORS pe toate căile
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
