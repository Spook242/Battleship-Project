package cat.itacademy.battleship_api.controller;

import cat.itacademy.battleship_api.model.Game;
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

    @PostMapping("/new")
    public ResponseEntity<Game> startGame(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        if (username == null || username.isBlank()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(gameService.createGame(username));
    }

    @PostMapping("/{gameId}/fire")
    public ResponseEntity<Game> fire(@PathVariable String gameId, @RequestBody Map<String, String> request) {
        String coordinate = request.get("coordinate");
        if (coordinate == null || coordinate.isBlank()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(gameService.playerMove(gameId, coordinate));
    }

    // 👇 LOG MESSAGE UPDATED TO ENGLISH 👇
    @PostMapping("/{gameId}/cpu-turn")
    public ResponseEntity<Game> cpuTurn(@PathVariable String gameId) {
        System.out.println("🤖 CPU: Received shooting order for game " + gameId);
        Game game = gameService.executeCpuTurn(gameId);
        return ResponseEntity.ok(game);
    }
}