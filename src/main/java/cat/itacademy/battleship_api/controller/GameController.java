package cat.itacademy.battleship_api.controller;

import cat.itacademy.battleship_api.dto.*;
import cat.itacademy.battleship_api.model.Game;
import cat.itacademy.battleship_api.model.Ship;
import cat.itacademy.battleship_api.service.GameService;
import io.swagger.v3.oas.annotations.Operation; // Opcional: Para documentación
import io.swagger.v3.oas.annotations.tags.Tag; // Opcional
import jakarta.validation.Valid; // Importante para validar inputs
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/game")
@CrossOrigin(origins = "*") // ⚠️ Nota: En producción real, cambia '*' por la URL de tu frontend
@RequiredArgsConstructor
@Tag(name = "Game Controller", description = "Controlador principal para la lógica del juego Battleship")
public class GameController {

    private final GameService gameService;

    // 1. CREAR PARTIDA
    @Operation(summary = "Inicia una nueva partida", description = "Recibe un usuario y devuelve el ID de la partida y el token JWT.")
    @PostMapping("/new")
    public ResponseEntity<GameStartResponse> createGame(@Valid @RequestBody StartGameRequest request) {
        GameStartResponse response = gameService.startNewGame(request.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 2. COLOCAR BARCOS E INICIAR BATALLA
    @Operation(summary = "Coloca los barcos e inicia la batalla", description = "Recibe la lista de barcos posicionados por el jugador.")
    @PostMapping("/{gameId}/start-battle")
    public ResponseEntity<Game> startBattle(
            @PathVariable String gameId,
            @RequestBody List<Ship> ships) {

        Game game = gameService.startBattle(gameId, ships);
        return ResponseEntity.ok(game);
    }

    // 3. DISPARAR (TURNO JUGADOR)
    @Operation(summary = "Realizar un disparo", description = "El jugador envía una coordenada (ej: 'A1') para atacar.")
    @PostMapping("/{gameId}/fire")
    public ResponseEntity<Game> playerFire(
            @PathVariable String gameId,
            @Valid @RequestBody FireRequest request) {

        Game game = gameService.playerMove(gameId, request.getCoordinate());
        return ResponseEntity.ok(game);
    }

    // 4. TURNO CPU
    @Operation(summary = "Turno de la CPU", description = "Solicita a la IA que realice su movimiento.")
    @PostMapping("/{gameId}/cpu-turn")
    public ResponseEntity<Game> cpuTurn(@PathVariable String gameId) {
        Game game = gameService.playCpuTurn(gameId);
        return ResponseEntity.ok(game);
    }

    // 5. RANKING
    @Operation(summary = "Obtener Ranking", description = "Devuelve la lista de mejores jugadores y sus victorias.")
    @GetMapping("/ranking")
    public ResponseEntity<List<PlayerScoreDTO>> getHallOfFame() {
        return ResponseEntity.ok(gameService.getRanking());
    }
}