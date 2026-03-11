package cat.itacademy.battleship_api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "secretKey",
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
    }


    @Test
    void generateToken_ShouldCreateValidToken_WithUsername() {
        String username = "ComandanteShepard";

        String token = jwtService.generateToken(username);

        assertNotNull(token, "El token no debe ser nulo");
        assertFalse(token.isEmpty(), "El token no debe estar vacío");

        String extractedUsername = jwtService.extractUsername(token);
        assertEquals(username, extractedUsername, "El usuario extraído debe coincidir con el original");
    }

    @Test
    void generateToken_ShouldIncludeExtraClaims() {
        String username = "Player1";
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("gameId", "Partida-999");
        extraClaims.put("role", "ADMIN");

        String token = jwtService.generateToken(extraClaims, username);

        assertTrue(jwtService.isTokenValid(token, username));

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

        String fakeUser = "Joker";
        assertFalse(jwtService.isTokenValid(token, fakeUser), "El token no debería valer para otro usuario");
    }
}