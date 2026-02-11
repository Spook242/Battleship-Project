package cat.itacademy.battleship_api.controller;

import cat.itacademy.battleship_api.dto.FireRequest;
import cat.itacademy.battleship_api.dto.GameStartResponse;
import cat.itacademy.battleship_api.dto.PlayerScoreDTO;
import cat.itacademy.battleship_api.dto.StartGameRequest;
import cat.itacademy.battleship_api.model.Game;
import cat.itacademy.battleship_api.model.Ship;
import cat.itacademy.battleship_api.security.JwtService;
import cat.itacademy.battleship_api.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/game")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final JwtService jwtService;

    @PostMapping("/new")
    public ResponseEntity<GameStartResponse> startGame(@RequestBody StartGameRequest request) {
        // Quitamos el if (username == null).
        // El GameService lanzará una excepción si el nombre no es válido.
        Game game = gameService.createGame(request.getUsername());

        String token = jwtService.generateToken(
                Map.of("gameId", game.getId()),
                request.getUsername()
        );

        return ResponseEntity.ok(new GameStartResponse(game, token));
    }

    @PostMapping("/{gameId}/start-battle")
    public ResponseEntity<Game> startBattle(@PathVariable String gameId, @RequestBody List<Ship> ships) {
        // Si el gameId no existe, el Service lanzará la excepción y aquí ni entra.
        return ResponseEntity.ok(gameService.startBattle(gameId, ships));
    }

    @PostMapping("/{gameId}/fire")
    public ResponseEntity<Game> fire(@PathVariable String gameId, @RequestBody FireRequest request) {
        // Quitamos el if (coordinate == null).
        // El Service se encarga de validar la coordenada.
        return ResponseEntity.ok(gameService.playerMove(gameId, request.getCoordinate()));
    }

    @PostMapping("/{gameId}/cpu-turn")
    public ResponseEntity<Game> cpuTurn(@PathVariable String gameId) {
        return ResponseEntity.ok(gameService.playCpuTurn(gameId));
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<PlayerScoreDTO>> getRanking() {
        return ResponseEntity.ok(gameService.getRanking());
    }
}