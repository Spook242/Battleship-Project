package cat.itacademy.battleship_api.controller;

import cat.itacademy.battleship_api.dto.FireRequest;
import cat.itacademy.battleship_api.dto.StartGameRequest;
import cat.itacademy.battleship_api.model.Game;
import cat.itacademy.battleship_api.security.JwtService;
import cat.itacademy.battleship_api.service.GameService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GameController.class)
@AutoConfigureMockMvc(addFilters = false) // Esto desactiva la seguridad (login) para simplificar el test del controlador puro
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc; // Simula las peticiones HTTP (Postman)

    @MockitoBean
    private GameService gameService; // Simulamos el servicio (la lógica real)

    @MockitoBean
    private JwtService jwtService; // Simulamos el servicio de tokens

    @Autowired
    private ObjectMapper objectMapper; // Convierte objetos Java a JSON y viceversa

    // --- TEST: START GAME ---

    @Test
    void startGame_ShouldReturnGameAndToken_WhenUsernameIsValid() throws Exception {
        // 1. PREPARACIÓN (GIVEN)
        String username = "Player1";
        StartGameRequest request = new StartGameRequest();
        request.setUsername(username);

        // Simulamos un juego devuelto por el servicio
        Game mockGame = new Game();
        mockGame.setId("game-123");
        // ... configura otros campos del juego si es necesario

        // Le decimos a Mockito: "Cuando alguien llame a createGame, devuelve este mockGame"
        when(gameService.createGame(username)).thenReturn(mockGame);
        // Le decimos a Mockito: "Cuando alguien pida un token, devuelve este String"
        when(jwtService.generateToken(any(), eq(username))).thenReturn("fake-jwt-token");

        // 2. EJECUCIÓN (WHEN) Y 3. VERIFICACIÓN (THEN)
        mockMvc.perform(post("/game/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))) // Convertimos el objeto a JSON
                .andExpect(status().isOk()) // Esperamos código 200
                .andExpect(jsonPath("$.game.id").value("game-123")) // Verificamos el JSON de respuesta
                .andExpect(jsonPath("$.token").value("fake-jwt-token"));
    }

    @Test
    void startGame_ShouldReturnBadRequest_WhenUsernameIsMissing() throws Exception {
        // 1. PREPARACIÓN: Request vacío
        StartGameRequest request = new StartGameRequest();
        // No seteamos usuario (es null)

        // 2. EJECUCIÓN Y VERIFICACIÓN
        mockMvc.perform(post("/game/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // Esperamos código 400
    }

    // --- TEST: FIRE ---

    @Test
    void fire_ShouldReturnUpdatedGame_WhenCoordinateIsValid() throws Exception {
        // 1. PREPARACIÓN
        String gameId = "game-123";
        FireRequest request = new FireRequest();
        request.setCoordinate("A5");

        Game updatedGame = new Game();
        updatedGame.setId(gameId);
        // Aquí podrías simular que el juego cambió de estado, pero para el controlador basta con que devuelva el objeto

        // Mock: Cuando llamen a playerMove con este ID y "A5", devuelve el juego
        when(gameService.playerMove(gameId, "A5")).thenReturn(updatedGame);

        // 2. EJECUCIÓN Y VERIFICACIÓN
        mockMvc.perform(post("/game/{gameId}/fire", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(gameId));
    }

    @Test
    void fire_ShouldReturnBadRequest_WhenCoordinateIsMissing() throws Exception {
        // 1. PREPARACIÓN
        FireRequest request = new FireRequest();
        // No seteamos coordenada

        // 2. EJECUCIÓN Y VERIFICACIÓN
        mockMvc.perform(post("/game/game-123/fire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // --- TEST: CPU TURN ---

    @Test
    void cpuTurn_ShouldReturnUpdatedGame() throws Exception {
        // 1. PREPARACIÓN
        String gameId = "game-123";
        Game gameAfterCpu = new Game();
        gameAfterCpu.setId(gameId);

        when(gameService.playCpuTurn(gameId)).thenReturn(gameAfterCpu);

        // 2. EJECUCIÓN Y VERIFICACIÓN
        mockMvc.perform(post("/game/{gameId}/cpu-turn", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(gameId));
    }
}