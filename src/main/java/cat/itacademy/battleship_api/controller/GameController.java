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

        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Game game = gameService.createGame(username);
        return ResponseEntity.ok(game);
    }
}