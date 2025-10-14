package com.barlog.loyaltyapi.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "La Bârlog Loyalty API", version = "v1"),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth", // Un nume de referință pentru schema de securitate
        scheme = "bearer",
        type = SecuritySchemeType.HTTP, // Tipul schemei este HTTP
        bearerFormat = "JWT", // Formatul token-ului
        in = SecuritySchemeIn.HEADER // Unde se află token-ul (în header)
)
public class OpenApiConfig {
}