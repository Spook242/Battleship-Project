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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GameController.class)
@AutoConfigureMockMvc(addFilters = false) 
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameService gameService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void startGame_ShouldReturnGameAndToken_WhenUsernameIsValid() throws Exception {

        String username = "Player1";
        GameStartRequest request = new GameStartRequest(username);

        Game mockGame = new Game();
        mockGame.setId("game-abc-123");
        mockGame.setPlayerId(1L); 

        GameStartResponse mockResponse = new GameStartResponse(mockGame, "fake-jwt-token");

        when(gameService.startNewGame(eq(username))).thenReturn(mockResponse);

        mockMvc.perform(post("/game/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.game.id").value("game-abc-123"))
                .andExpect(jsonPath("$.token").value("fake-jwt-token"));
    }

    @Test
    void startGame_ShouldReturnBadRequest_WhenUsernameIsMissing() throws Exception {
        GameStartRequest request = new GameStartRequest();

        mockMvc.perform(post("/game/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void fire_ShouldReturnUpdatedGame_WhenCoordinateIsValid() throws Exception {
        String gameId = "game-abc-123";
        FireRequest request = new FireRequest();
        request.setCoordinate("A5");

        Game updatedGame = new Game();
        updatedGame.setId(gameId);

        when(gameService.playerMove(eq(gameId), eq("A5"))).thenReturn(updatedGame);

        mockMvc.perform(post("/game/{gameId}/fire", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(gameId));
    }

    @Test
    void fire_ShouldReturnBadRequest_WhenCoordinateIsMissing() throws Exception {
        FireRequest request = new FireRequest();

        mockMvc.perform(post("/game/game-abc-123/fire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void cpuTurn_ShouldReturnUpdatedGame() throws Exception {
        String gameId = "game-abc-123";
        Game gameAfterCpu = new Game();
        gameAfterCpu.setId(gameId);

        when(gameService.playCpuTurn(eq(gameId))).thenReturn(gameAfterCpu);

        mockMvc.perform(post("/game/{gameId}/cpu-turn", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(gameId));
    }
}