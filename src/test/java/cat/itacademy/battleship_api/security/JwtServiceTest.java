package cat.itacademy.battleship_api.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        // Al ser un servicio sin dependencias externas (no usa repositorios),
        // podemos instanciarlo con 'new' directamente.
        jwtService = new JwtService();
    }

    @Test
    void generateToken_ShouldCreateValidToken_WithUsername() {
        // 1. GIVEN (Dado un usuario)
        String username = "ComandanteShepard";

        // 2. WHEN (Cuando generamos el token)
        String token = jwtService.generateToken(username);

        // 3. THEN (Entonces verificamos)
        assertNotNull(token, "El token no debe ser nulo");
        assertFalse(token.isEmpty(), "El token no debe estar vacío");

        // Verificamos que podemos recuperar el usuario del token
        String extractedUsername = jwtService.extractUsername(token);
        assertEquals(username, extractedUsername, "El usuario extraído debe coincidir con el original");
    }

    @Test
    void generateToken_ShouldIncludeExtraClaims() {
        // 1. GIVEN
        String username = "Player1";
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("gameId", "Partida-999");
        extraClaims.put("role", "ADMIN");

        // 2. WHEN
        String token = jwtService.generateToken(extraClaims, username);

        // 3. THEN
        // Verificamos que el token es válido
        assertTrue(jwtService.isTokenValid(token, username));

        // Para verificar un claim personalizado, usamos tu método extractClaim
        // Esto le dice a Java: "Dame todos los claims y selecciona el que se llame 'gameId'"
        String gameId = jwtService.extractClaim(token, claims -> claims.get("gameId", String.class));

        assertEquals("Partida-999", gameId, "El token debe contener el gameId correcto");
    }

    @Test
    void isTokenValid_ShouldReturnTrue_WhenUserMatches() {
        String username = "CapitanHaddock";
        String token = jwtService.generateToken(username);

        assertTrue(jwtService.isTokenValid(token, username));
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenUserDoesNotMatch() {
        String username = "Batman";
        String token = jwtService.generateToken(username);

        String fakeUser = "Joker"; // Intentamos validar con otro usuario
        assertFalse(jwtService.isTokenValid(token, fakeUser), "El token no debería valer para otro usuario");
    }
}