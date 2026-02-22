package cat.itacademy.battleship_api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Battleship API",
                version = "1.0",
                description = "Battleship game API documentation"
        ),
        // Esto aplica la seguridad por defecto a todos los endpoints
        security = @SecurityRequirement(name = "bearerAuth")
)
// Esto define CÓMO es la seguridad (Tipo JWT)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
    // No hace falta poner código dentro, las anotaciones hacen la magia
}