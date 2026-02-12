package cat.itacademy.battleship_api.controller;

import cat.itacademy.battleship_api.dto.*;
import cat.itacademy.battleship_api.model.Game;
import cat.itacademy.battleship_api.model.Ship; // Importar Ship si usas List<Ship>
import cat.itacademy.battleship_api.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map; // Necesario si el JS manda JSON planos

@RestController
@RequestMapping("/game") // 1. Mantenemos "/game" para no romper el JS
@CrossOrigin(origins = "*") // Importante para evitar bloqueos CORS
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    // POST /game/new
    @PostMapping("/new")
    public ResponseEntity<GameStartResponse> createGame(@RequestBody StartGameRequest request) {
        // Asumiendo que StartGameRequest tiene un campo "username"
        return new ResponseEntity<>(gameService.startNewGame(request.getUsername()), HttpStatus.CREATED);
    }

    // POST /game/{id}/start-battle (El JS llama a esto, no a /ships)
    @PostMapping("/{gameId}/start-battle")
    public ResponseEntity<Game> placeShips(@PathVariable String gameId, @RequestBody List<Ship> ships) {
        // El JS envía una lista de barcos directa, no un objeto "PlaceShipsRequest"
        return ResponseEntity.ok(gameService.startBattle(gameId, ships));
    }

    // POST /game/{id}/fire
    @PostMapping("/{gameId}/fire")
    public ResponseEntity<Game> fireShot(@PathVariable String gameId, @RequestBody FireRequest request) {
        // Asegúrate de que FireRequest tenga el campo "coordinate"
        return ResponseEntity.ok(gameService.playerMove(gameId, request.getCoordinate()));
    }

    // POST /game/{id}/cpu-turn
    @PostMapping("/{gameId}/cpu-turn")
    public ResponseEntity<Game> playCpuTurn(@PathVariable String gameId) {
        return ResponseEntity.ok(gameService.playCpuTurn(gameId));
    }

    // GET /game/ranking
    @GetMapping("/ranking")
    public ResponseEntity<List<PlayerScoreDTO>> getRanking() {
        return ResponseEntity.ok(gameService.getRanking());
    }
}