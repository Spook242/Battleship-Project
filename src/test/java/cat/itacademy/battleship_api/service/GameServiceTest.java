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

// @ExtendWith habilita el uso de Mockito en esta clase
@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    // 1. MOCK: Simulamos los repositorios (no queremos conectar a la BD real)
    @Mock
    private GameRepository gameRepository;

    @Mock
    private PlayerRepository playerRepository;

    // 2. INJECT MOCKS: Inyectamos los mocks dentro de nuestro servicio real
    @InjectMocks
    private GameService gameService;

    // Variables auxiliares para los tests
    private Game juegoPrueba;
    private Player jugadorPrueba;

    @BeforeEach
    void setUp() {
        // Configuramos datos básicos antes de cada test
        jugadorPrueba = new Player("CapitanJack");
        jugadorPrueba.setId(1L);

        juegoPrueba = Game.builder()
                .id("game-123")
                .playerId(jugadorPrueba.getId())
                .status("PLAYING")
                .turn("PLAYER") // Empieza el jugador
                .playerBoard(new Board())
                .cpuBoard(new Board())
                .build();
    }

    // ==========================================
    // TEST 1: CREAR JUEGO
    // ==========================================
    @Test
    @DisplayName("createGame debería inicializar tableros y colocar barcos")
    void testCreateGame() {
        // GIVEN (Dado que...): Simulamos que el repositorio encuentra o guarda al jugador
        when(playerRepository.findByUsername("CapitanJack")).thenReturn(Optional.of(jugadorPrueba));
        // Simulamos que al guardar el juego, nos devuelve el mismo juego
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN (Cuando...): Llamamos al método real
        Game nuevoJuego = gameService.createGame("CapitanJack");

        // THEN (Entonces...): Verificaciones
        assertNotNull(nuevoJuego);
        assertEquals("PLAYING", nuevoJuego.getStatus());
        assertEquals("PLAYER", nuevoJuego.getTurn());

        // Verificar que se han colocado barcos automáticamente
        assertFalse(nuevoJuego.getPlayerBoard().getShips().isEmpty(), "El tablero del jugador debería tener barcos");
        assertFalse(nuevoJuego.getCpuBoard().getShips().isEmpty(), "El tablero de la CPU debería tener barcos");

        // Verificar que se llamó a guardar en la BD
        verify(gameRepository, times(1)).save(any(Game.class));
    }

    // ==========================================
    // TEST 2: DISPARO DEL JUGADOR - AGUA
    // ==========================================
    @Test
    @DisplayName("playerMove debería cambiar turno a CPU si el disparo es AGUA")
    void testPlayerMove_Miss() {
        // GIVEN: Juego existente donde la CPU no tiene barcos en A1
        when(gameRepository.findById("game-123")).thenReturn(Optional.of(juegoPrueba));
        when(gameRepository.save(any(Game.class))).thenReturn(juegoPrueba);

        // WHEN: Disparamos a "A1" (asumimos que está vacío por defecto)
        Game resultado = gameService.playerMove("game-123", "A1");

        // THEN: El turno debe cambiar a CPU
        assertEquals("CPU", resultado.getTurn());
        assertTrue(resultado.getCpuBoard().getShotsReceived().contains("A1"));
    }

    // ==========================================
    // TEST 3: DISPARO DEL JUGADOR - IMPACTO
    // ==========================================
    @Test
    @DisplayName("playerMove debería mantener turno PLAYER si el disparo es IMPACTO")
    void testPlayerMove_Hit() {
        // GIVEN: Colocamos un barco enemigo en A1 manualmente para probar
        Ship barcoEnemigo = new Ship("TestShip", 1, List.of("A1"), new ArrayList<>(), false);
        juegoPrueba.getCpuBoard().getShips().add(barcoEnemigo);

        when(gameRepository.findById("game-123")).thenReturn(Optional.of(juegoPrueba));
        when(gameRepository.save(any(Game.class))).thenReturn(juegoPrueba);

        // WHEN: Disparamos a "A1"
        Game resultado = gameService.playerMove("game-123", "A1");

        // THEN: El turno debe seguir siendo PLAYER (premio por acertar)
        assertEquals("PLAYER", resultado.getTurn());
        // El barco debe tener un impacto
        assertTrue(barcoEnemigo.getHits().contains("A1"));
        assertTrue(barcoEnemigo.isSunk()); // Al ser tamaño 1, debe hundirse
    }

    // ==========================================
    // TEST 4: VALIDACIONES (Turno Incorrecto)
    // ==========================================
    @Test
    @DisplayName("playerMove debería lanzar excepción si no es turno del jugador")
    void testPlayerMove_WrongTurn() {
        // GIVEN: Configuramos el juego para que sea turno de la CPU
        juegoPrueba.setTurn("CPU");
        when(gameRepository.findById("game-123")).thenReturn(Optional.of(juegoPrueba));

        // WHEN & THEN: Esperamos que lance RuntimeException
        Exception exception = assertThrows(RuntimeException.class, () -> {
            gameService.playerMove("game-123", "A1");
        });

        assertTrue(exception.getMessage().contains("It's not your turn"));
    }

    // ==========================================
    // TEST 5: IA DE LA CPU
    // ==========================================
    @Test
    @DisplayName("playCpuTurn debería disparar y guardar cambios")
    void testPlayCpuTurn() {
        // GIVEN
        juegoPrueba.setTurn("CPU"); // Es turno de la máquina
        when(gameRepository.findById("game-123")).thenReturn(Optional.of(juegoPrueba));
        when(gameRepository.save(any(Game.class))).thenReturn(juegoPrueba);

        // WHEN
        Game resultado = gameService.playCpuTurn("game-123");

        // THEN
        assertNotNull(resultado);
        // Verificar que la lista de disparos recibidos por el jugador aumentó en 1
        assertEquals(1, resultado.getPlayerBoard().getShotsReceived().size());

        // Verificar que se guardó en BD
        verify(gameRepository, times(1)).save(juegoPrueba);
    }

    // ==========================================
    // TEST 6: GANAR PARTIDA
    // ==========================================
    @Test
    @DisplayName("Debe detectar ganador cuando se hunden todos los barcos")
    void testCheckWinner() {
        // GIVEN: Juego listo
        when(gameRepository.findById("game-123")).thenReturn(Optional.of(juegoPrueba));
        when(gameRepository.save(any(Game.class))).thenReturn(juegoPrueba);

        // ESCENARIO: La CPU solo tiene 1 barco de tamaño 1 en "B5"
        Ship barcoCpu = new Ship("MiniBarco", 1, List.of("B5"), new ArrayList<>(), false);
        juegoPrueba.getCpuBoard().setShips(new ArrayList<>(List.of(barcoCpu)));

        // WHEN: El jugador dispara a B5 (Último barco)
        Game resultado = gameService.playerMove("game-123", "B5");

        // THEN: El estado debe ser FINISHED y ganador PLAYER
        assertEquals("FINISHED", resultado.getStatus());
        assertEquals("PLAYER", resultado.getWinner());
    }
}