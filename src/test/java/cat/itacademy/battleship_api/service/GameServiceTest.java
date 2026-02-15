package cat.itacademy.battleship_api.service;

import cat.itacademy.battleship_api.dto.GameStartResponse;
import cat.itacademy.battleship_api.model.Game;
import cat.itacademy.battleship_api.model.Player;
import cat.itacademy.battleship_api.repository.GameRepository;
import cat.itacademy.battleship_api.repository.PlayerRepository;
import cat.itacademy.battleship_api.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private BoardService boardService; // Necesario porque startNewGame lo usa
    @Mock
    private JwtService jwtService;     // Necesario para el token

    @InjectMocks
    private GameService gameService;

    @Test
    void testStartNewGame() { // Renombrado de testCreateGame
        // GIVEN
        String username = "CapitanJack";
        Player jugadorPrueba = new Player(username);
        jugadorPrueba.setId(1L);

        when(playerRepository.findByUsername(username)).thenReturn(Optional.of(jugadorPrueba));

        // Simulamos que al guardar, devuelve el juego con un ID de Mongo falso
        when(gameRepository.save(any(Game.class))).thenAnswer(i -> {
            Game g = i.getArgument(0);
            g.setId("mongo-id-123"); // ID tipo String
            return g;
        });

        // Simulamos la generación del token
        when(jwtService.generateToken(any(), any())).thenReturn("fake-token");

        // WHEN
        // 👇 CAMBIO: Usamos startNewGame en lugar de createGame
        GameStartResponse response = gameService.startNewGame(username);
        Game nuevoJuego = response.getGame();

        // THEN
        assertNotNull(nuevoJuego);
        assertEquals("SETUP", nuevoJuego.getStatus()); // Ahora empieza en SETUP, no PLAYING
        assertEquals("mongo-id-123", nuevoJuego.getId());

        // Verificamos que se llamó al repositorio
        verify(gameRepository).save(any(Game.class));
    }
}