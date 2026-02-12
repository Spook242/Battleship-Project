package cat.itacademy.battleship_api.controller;

import cat.itacademy.battleship_api.dto.*;
import cat.itacademy.battleship_api.model.Game;
import cat.itacademy.battleship_api.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games") // 1. Convención: Plural y prefijo /api
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    // Eliminamos JwtService de aquí: El controlador no debe saber de criptografía.

    // POST /api/games -> Crea un recurso nuevo (Partida)
    @PostMapping
    public ResponseEntity<GameStartResponse> createGame(@RequestBody StartGameRequest request) {
        // Toda la lógica de creación + token se mueve al servicio
        return new ResponseEntity<>(gameService.startNewGame(request.getUsername()), HttpStatus.CREATED);
    }

    // PUT /api/games/{id}/ships -> Modifica el estado "barcos" del recurso
    @PutMapping("/{gameId}/ships")
    public ResponseEntity<Game> placeShips(@PathVariable String gameId, @RequestBody PlaceShipsRequest request) {
        // 2. Usamos un DTO 'wrapper' en lugar de una lista cruda
        return ResponseEntity.ok(gameService.startBattle(gameId, request.getShips()));
    }

    // POST /api/games/{id}/shots -> Crea un recurso "disparo" (o acción de disparo)
    @PostMapping("/{gameId}/shots")
    public ResponseEntity<Game> fireShot(@PathVariable String gameId, @RequestBody FireRequest request) {
        return ResponseEntity.ok(gameService.playerMove(gameId, request.getCoordinate()));
    }

    // POST /api/games/{id}/cpu-turn -> Fuerza el turno de la CPU
    @PostMapping("/{gameId}/cpu-turn")
    public ResponseEntity<Game> playCpuTurn(@PathVariable String gameId) {
        return ResponseEntity.ok(gameService.playCpuTurn(gameId));
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<PlayerScoreDTO>> getRanking() {
        return ResponseEntity.ok(gameService.getRanking());
    }
}