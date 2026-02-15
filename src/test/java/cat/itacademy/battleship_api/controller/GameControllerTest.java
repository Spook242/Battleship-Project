package cat.itacademy.battleship_api.controller;

import cat.itacademy.battleship_api.dto.FireRequest;
import cat.itacademy.battleship_api.dto.GameStartRequest;
import cat.itacademy.battleship_api.dto.GameStartResponse;
import cat.itacademy.battleship_api.model.Game;
import cat.itacademy.battleship_api.security.JwtService;
import cat.itacademy.battleship_api.service.GameService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // Nota: MockitoBean es para Spring Boot 3.4+. Si usas anterior, usa @MockBean
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GameController.class)
@AutoConfigureMockMvc(addFilters = false) // Desactiva seguridad para testear solo lógica
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameService gameService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    // --- TEST: START GAME ---

    @Test
    void startGame_ShouldReturnGameAndToken_WhenUsernameIsValid() throws Exception {

        // 1. PREPARACIÓN DE DATOS
        String username = "Player1";
        // CORRECCIÓN: Usamos GameStartRequest, no StartGameRequest
        GameStartRequest request = new GameStartRequest(username);

        // Creamos el juego falso (Mock)
        Game mockGame = new Game();
        // CORRECCIÓN: MongoDB usa IDs String. Ponemos un texto, no un número.
        mockGame.setId("game-abc-123");
        mockGame.setPlayerId(1L); // El PlayerID puede ser Long si así lo definiste en Player

        // Respuesta esperada del servicio
        GameStartResponse mockResponse = new GameStartResponse(mockGame, "fake-jwt-token");

        // 2. SIMULACIÓN DEL SERVICIO
        when(gameService.startNewGame(eq(username))).thenReturn(mockResponse);

        // 3. EJECUCIÓN Y VERIFICACIÓN
        mockMvc.perform(post("/game/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                // CORRECCIÓN: Esperamos el String "game-abc-123"
                .andExpect(jsonPath("$.game.id").value("game-abc-123"))
                .andExpect(jsonPath("$.token").value("fake-jwt-token"));
    }

    @Test
    void startGame_ShouldReturnBadRequest_WhenUsernameIsMissing() throws Exception {
        // 1. PREPARACIÓN: Request vacío
        // CORRECCIÓN: Usamos la clase correcta GameStartRequest
        GameStartRequest request = new GameStartRequest();
        // Username es null por defecto

        // 2. EJECUCIÓN
        mockMvc.perform(post("/game/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // Espera 400 Bad Request
    }

    // --- TEST: FIRE ---

    @Test
    void fire_ShouldReturnUpdatedGame_WhenCoordinateIsValid() throws Exception {
        // 1. PREPARACIÓN
        String gameId = "game-abc-123";
        FireRequest request = new FireRequest();
        request.setCoordinate("A5");

        Game updatedGame = new Game();
        updatedGame.setId(gameId);

        // Mock: Cuando llamen a playerMove con este ID string y "A5", devuelve el juego
        when(gameService.playerMove(eq(gameId), eq("A5"))).thenReturn(updatedGame);

        // 2. EJECUCIÓN
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
        // No seteamos coordenada (null)

        // 2. EJECUCIÓN
        mockMvc.perform(post("/game/game-abc-123/fire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // --- TEST: CPU TURN ---

    @Test
    void cpuTurn_ShouldReturnUpdatedGame() throws Exception {
        // 1. PREPARACIÓN
        String gameId = "game-abc-123";
        Game gameAfterCpu = new Game();
        gameAfterCpu.setId(gameId);

        when(gameService.playCpuTurn(eq(gameId))).thenReturn(gameAfterCpu);

        // 2. EJECUCIÓN
        mockMvc.perform(post("/game/{gameId}/cpu-turn", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(gameId));
    }
}