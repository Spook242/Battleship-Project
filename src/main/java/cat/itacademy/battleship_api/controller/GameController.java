package cat.itacademy.battleship_api.controller;

import cat.itacademy.battleship_api.dto.GameStartResponse;
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
    // 2. Cambiamos el tipo de retorno a GameStartResponse (o ResponseEntity<?>)
    public ResponseEntity<GameStartResponse> startGame(@RequestBody Map<String, String> request) {
        String username = request.get("username");

        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        // Creamos el juego
        Game game = gameService.createGame(username);

        // 3. Generamos el token usando el usuario y el ID del juego
        String token = jwtService.generateToken(username, game.getId());

        // 4. Devolvemos ambos objetos envueltos en el DTO
        return ResponseEntity.ok(new GameStartResponse(game, token));
    }

    // ... el resto de métodos (fire, cpu-turn) siguen igual por ahora ...
    @PostMapping("/{gameId}/fire")
    public ResponseEntity<Game> fire(@PathVariable String gameId, @RequestBody Map<String, String> request) {
        String coordinate = request.get("coordinate");
        if (coordinate == null || coordinate.isBlank()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(gameService.playerMove(gameId, coordinate));
    }

    @PostMapping("/{gameId}/cpu-turn")
    public ResponseEntity<Game> cpuTurn(@PathVariable String gameId) {
        System.out.println("🤖 CPU: Received shooting order for game " + gameId);
        Game game = gameService.playCpuTurn(gameId);
        return ResponseEntity.ok(game);
    }
}