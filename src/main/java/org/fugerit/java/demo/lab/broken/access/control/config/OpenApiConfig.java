package org.fugerit.java.demo.lab.broken.access.control.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Configurazione OpenAPI (Swagger UI) per il laboratorio.
 *
 * Dichiara lo schema di autenticazione JWT Bearer che viene usato
 * da tutti gli endpoint protetti tramite @SecurityRequirement(name = "bearerAuth")
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Broken Access Control Lab API",
                version = "1.0.1",
                description = "Laboratorio educativo per vulnerabilità Broken Access Control con Spring Boot"
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT Bearer Token Authentication. Usa l'endpoint /demo/{roles}.txt per generare un token."
)
public class OpenApiConfig {
}