package cat.itacademy.battleship_api.service;

import cat.itacademy.battleship_api.model.Board;
import cat.itacademy.battleship_api.model.Game;
import cat.itacademy.battleship_api.model.Player;
import cat.itacademy.battleship_api.model.Ship;
import cat.itacademy.battleship_api.repository.GameRepository;
import cat.itacademy.battleship_api.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private GameService gameService;

    private Game juegoPrueba;
    private Player jugadorPrueba;

    @BeforeEach
    void setUp() {
        jugadorPrueba = new Player("CapitanJack");
        jugadorPrueba.setId(1L);

        // Inicializamos un juego limpio antes de cada test
        juegoPrueba = Game.builder()
                .id("game-123")
                .playerId(jugadorPrueba.getId())
                .status("PLAYING")
                .turn("PLAYER")
                .playerBoard(new Board()) // Tablero vacío
                .cpuBoard(new Board())    // Tablero vacío
                .build();
    }

    @Test
    @DisplayName("createGame: Debe crear un juego nuevo con barcos colocados")
    void testCreateGame() {
        // GIVEN
        when(playerRepository.findByUsername("CapitanJack")).thenReturn(Optional.of(jugadorPrueba));
        // Simulamos que al guardar devuelve el objeto que le pasamos
        when(gameRepository.save(any(Game.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        Game nuevoJuego = gameService.createGame("CapitanJack");

        // THEN
        assertNotNull(nuevoJuego);
        assertEquals("PLAYING", nuevoJuego.getStatus());
        // Verificamos que la lógica de 'placeShipsRandomly' ha funcionado (hay barcos en las listas)
        assertFalse(nuevoJuego.getPlayerBoard().getShips().isEmpty(), "El jugador debe tener barcos");
        assertFalse(nuevoJuego.getCpuBoard().getShips().isEmpty(), "La CPU debe tener barcos");

        verify(gameRepository).save(any(Game.class));
    }

    @Test
    @DisplayName("playerMove (AGUA): Debe cambiar el turno a CPU")
    void testPlayerMove_Miss() {
        // GIVEN
        when(gameRepository.findById("game-123")).thenReturn(Optional.of(juegoPrueba));
        when(gameRepository.save(any(Game.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN: Disparamos a A1 (Asumimos que está vacío)
        Game resultado = gameService.playerMove("game-123", "A1");

        // THEN
        assertEquals("CPU", resultado.getTurn(), "Si fallas, pasa el turno a la CPU");
        assertTrue(resultado.getCpuBoard().getShotsReceived().contains("A1"));
    }

    @Test
    @DisplayName("playerMove (IMPACTO): El jugador debe repetir turno")
    void testPlayerMove_Hit() {
        // GIVEN: Ponemos un barco enemigo manualmente en A1
        Ship barco = new Ship("Bote", 1, List.of("A1"), new ArrayList<>(), false);
        juegoPrueba.getCpuBoard().getShips().add(barco);

        when(gameRepository.findById("game-123")).thenReturn(Optional.of(juegoPrueba));
        when(gameRepository.save(any(Game.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        Game resultado = gameService.playerMove("game-123", "A1");

        // THEN
        assertEquals("PLAYER", resultado.getTurn(), "Si aciertas, repites turno");
        assertTrue(barco.getHits().contains("A1"));
        assertTrue(barco.isSunk(), "El barco de tamaño 1 debería hundirse");
    }

    @Test
    @DisplayName("playerMove: Debe lanzar error si no es tu turno")
    void testPlayerMove_WrongTurn() {
        // GIVEN
        juegoPrueba.setTurn("CPU"); // Es turno de la máquina
        when(gameRepository.findById("game-123")).thenReturn(Optional.of(juegoPrueba));

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameService.playerMove("game-123", "A1");
        });

        assertEquals("It's not your turn! Wait for the CPU!", exception.getMessage());
    }

    @Test
    @DisplayName("playCpuTurn: La CPU debe disparar y guardar el estado")
    void testPlayCpuTurn() {
        // GIVEN
        juegoPrueba.setTurn("CPU");
        when(gameRepository.findById("game-123")).thenReturn(Optional.of(juegoPrueba));
        when(gameRepository.save(any(Game.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        Game resultado = gameService.playCpuTurn("game-123");

        // THEN
        assertNotNull(resultado);
        // Comprobamos que la lista de disparos recibidos por el jugador ha aumentado
        assertEquals(1, resultado.getPlayerBoard().getShotsReceived().size(), "La CPU debería haber disparado una vez");

        // Verificamos que se llamó al repositorio para guardar
        verify(gameRepository, times(1)).save(any(Game.class));
    }

    @Test
    @DisplayName("CheckWinner: Debe detectar cuando el jugador gana")
    void testCheckWinner() {
        // GIVEN
        Ship barcoCpu = new Ship("Bote", 1, List.of("B5"), new ArrayList<>(), false);
        juegoPrueba.getCpuBoard().getShips().add(barcoCpu);

        when(gameRepository.findById("game-123")).thenReturn(Optional.of(juegoPrueba));
        when(gameRepository.save(any(Game.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN: Disparamos al único barco que queda
        Game resultado = gameService.playerMove("game-123", "B5");

        // THEN
        assertEquals("FINISHED", resultado.getStatus());
        assertEquals("PLAYER", resultado.getWinner());
    }
}