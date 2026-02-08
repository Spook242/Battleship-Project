package cat.itacademy.battleship_api.integration;

import cat.itacademy.battleship_api.dto.FireRequest;
import cat.itacademy.battleship_api.dto.StartGameRequest;
import cat.itacademy.battleship_api.model.Game;
import cat.itacademy.battleship_api.repository.GameRepository;
import cat.itacademy.battleship_api.repository.PlayerRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
// 👇 CAMBIO CLAVE: Usamos tu Docker (27017) pero en una DB separada
@TestPropertySource(properties = {
        "spring.data.mongodb.host=localhost",
        "spring.data.mongodb.port=27017",         // Puerto REAL de tu Docker
        "spring.data.mongodb.database=battleship_test_db", // Base de datos SOLO para tests
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class GameIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Esto borrará la base de datos "battleship_test_db" antes de cada test.
        // Tus datos reales en "battleship_game" están a salvo.
        gameRepository.deleteAll();
        playerRepository.deleteAll();
    }

    @Test
    @DisplayName("Integración: Crear Partida -> Obtener Token -> Disparar y Guardar en Mongo")
    void fullGameFlow() throws Exception {

        // --- PASO 1: LOGIN / START ---
        StartGameRequest startRequest = new StartGameRequest();
        startRequest.setUsername("TestUserIntegration");

        MvcResult result = mockMvc.perform(post("/game/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        String token = jsonNode.get("token").asText();
        String gameId = jsonNode.get("game").get("id").asText();

        // --- PASO 2: DISPARAR (FIRE) ---
        FireRequest fireRequest = new FireRequest();
        fireRequest.setCoordinate("A1");

        mockMvc.perform(post("/game/" + gameId + "/fire")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fireRequest)))
                .andExpect(status().isOk());

        // --- PASO 3: VERIFICAR ---
        Game gameSaved = gameRepository.findById(gameId).orElseThrow();
        assertTrue(gameSaved.getCpuBoard().getShotsReceived().contains("A1"));
    }
}