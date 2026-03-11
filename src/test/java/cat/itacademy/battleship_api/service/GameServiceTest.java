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
    private BoardService boardService;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private GameService gameService;

    @Test
    void testStartNewGame() {
        String username = "CapitanJack";
        Player jugadorPrueba = new Player(username);
        jugadorPrueba.setId(1L);

        when(playerRepository.findByUsername(username)).thenReturn(Optional.of(jugadorPrueba));

        when(gameRepository.save(any(Game.class))).thenAnswer(i -> {
            Game g = i.getArgument(0);
            g.setId("mongo-id-123");
            return g;
        });

        when(jwtService.generateToken(any(), any())).thenReturn("fake-token");

        GameStartResponse response = gameService.startNewGame(username);
        Game nuevoJuego = response.getGame();

        assertNotNull(nuevoJuego);
        assertEquals("SETUP", nuevoJuego.getStatus());
        assertEquals("mongo-id-123", nuevoJuego.getId());

        verify(gameRepository).save(any(Game.class));
    }
}