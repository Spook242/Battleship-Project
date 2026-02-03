package cat.itacademy.battleship_api.controller;

import cat.itacademy.battleship_api.dto.FireRequest;
import cat.itacademy.battleship_api.dto.GameStartResponse;
import cat.itacademy.battleship_api.dto.StartGameRequest;
import cat.itacademy.battleship_api.model.Game;
import cat.itacademy.battleship_api.security.JwtService;
import cat.itacademy.battleship_api.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/game")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final JwtService jwtService;

    @PostMapping("/new")
    public ResponseEntity<GameStartResponse> startGame(@RequestBody StartGameRequest request) {

        String username = request.getUsername();

        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Game game = gameService.createGame(username);
        String token = jwtService.generateToken(username, game.getId());

        return ResponseEntity.ok(new GameStartResponse(game, token));
    }

    // ... el resto de métodos (fire, cpu-turn) siguen igual por ahora ...
    @PostMapping("/{gameId}/fire")
    // 👇 CAMBIO: Usamos FireRequest en lugar de Map
    public ResponseEntity<Game> fire(@PathVariable String gameId, @RequestBody FireRequest request) {

        // 👇 CAMBIO: Obtenemos el dato con el getter
        String coordinate = request.getCoordinate();

        if (coordinate == null || coordinate.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(gameService.playerMove(gameId, coordinate));
    }

    @PostMapping("/{gameId}/cpu-turn")
    public ResponseEntity<Game> cpuTurn(@PathVariable String gameId) {
        System.out.println("🤖 CPU: Received shooting order for game " + gameId);
        Game game = gameService.playCpuTurn(gameId);
        return ResponseEntity.ok(game);
    }
}