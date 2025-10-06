package com.barlog.loyaltyapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Aplicăm regula pentru toate endpoint-urile care încep cu /api/
                .allowedOrigins("http://localhost:5173") // Permitem cereri de la adresa frontend-ului
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Permitem aceste metode HTTP
                .allowedHeaders("*") // Permitem toate header-ele
                .allowCredentials(true); // Permitem trimiterea de credențiale (ex: cookie-uri, deși nu le folosim încă)
    }
}