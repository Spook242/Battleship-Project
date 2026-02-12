package cat.itacademy.battleship_api.service;

import cat.itacademy.battleship_api.dto.GameStartResponse;
import cat.itacademy.battleship_api.dto.PlayerScoreDTO;
import cat.itacademy.battleship_api.exception.InvalidGameActionException;
import cat.itacademy.battleship_api.model.Board;
import cat.itacademy.battleship_api.model.Game;
import cat.itacademy.battleship_api.model.Player;
import cat.itacademy.battleship_api.model.Ship;
import cat.itacademy.battleship_api.repository.GameRepository;
import cat.itacademy.battleship_api.repository.PlayerRepository;
import cat.itacademy.battleship_api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final JwtService jwtService;

    // 👇 Inyectamos los nuevos servicios
    private final BattleshipAiService aiService;
    private final BoardService boardService;

    // ==========================================
    // 1. CREAR JUEGO
    // ==========================================
    public Game createGame(String username) {
        Player player = playerRepository.findByUsername(username)
                .orElseGet(() -> playerRepository.save(new Player(username)));

        Game game = Game.builder()
                .playerId(player.getId())
                .status("SETUP")
                .turn("PLAYER")
                .playerBoard(new Board())
                .cpuBoard(new Board())
                .build();

        // Delegamos la colocación de barcos al BoardService
        boardService.placeShipsRandomly(game.getCpuBoard());

        return gameRepository.save(game);
    }

    public GameStartResponse startNewGame(String username) {
        if (username == null || username.isBlank()) {
            throw new InvalidGameActionException("Username is required");
        }
        Game game = createGame(username);
        String token = jwtService.generateToken(Map.of("gameId", game.getId()), username);
        return new GameStartResponse(game, token);
    }

    public Game startBattle(String gameId, List<Ship> playerShips) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        if (playerShips.size() != 5) {
            throw new RuntimeException("You must place all 5 ships!");
        }

        game.getPlayerBoard().setShips(playerShips);
        game.setStatus("PLAYING");

        return gameRepository.save(game);
    }

    // ==========================================
    // 2. TURNO DEL JUGADOR
    // ==========================================
    public Game playerMove(String gameId, String coordinate) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        if (!game.getStatus().equals("PLAYING")) throw new RuntimeException("Game not active");
        if (!game.getTurn().equals("PLAYER")) throw new RuntimeException("Not your turn");

        // Delegamos el disparo
        boolean hit = boardService.processShot(game.getCpuBoard(), coordinate);

        if (!hit) {
            game.setTurn("CPU");
        }
        checkWinner(game);

        return gameRepository.save(game);
    }

    // ==========================================
    // 3. TURNO DE LA CPU
    // ==========================================
    public Game playCpuTurn(String gameId) {
        Game game = gameRepository.findById(gameId).orElse(null);
        if (game == null || !game.getStatus().equals("PLAYING")) return game;

        // 1. Delegamos el pensamiento a la IA
        String targetCoordinate = aiService.calculateCpuTarget(game.getPlayerBoard());

        // 2. Delegamos la acción de disparo
        boolean isHit = boardService.processShot(game.getPlayerBoard(), targetCoordinate);

        if (!isHit) {
            game.setTurn("PLAYER");
        }

        checkWinner(game);
        return gameRepository.save(game);
    }

    // ==========================================
    // RANKING Y LOGICA COMÚN
    // ==========================================
    private void checkWinner(Game game) {
        boolean allCpuSunk = game.getCpuBoard().getShips().stream().allMatch(Ship::isSunk);
        if (allCpuSunk) {
            game.setWinner("PLAYER");
            game.setStatus("FINISHED");
            return;
        }
        boolean allPlayerSunk = game.getPlayerBoard().getShips().stream().allMatch(Ship::isSunk);
        if (allPlayerSunk) {
            game.setWinner("CPU");
            game.setStatus("FINISHED");
        }
    }

    public List<PlayerScoreDTO> getRanking() {
        return playerRepository.findAll().stream()
                .map(player -> {
                    long wins = gameRepository.countByPlayerIdAndWinner(player.getId(), "PLAYER");
                    return new PlayerScoreDTO(player.getUsername(), wins);
                })
                .filter(score -> score.getWins() > 0)
                .sorted((a, b) -> Long.compare(b.getWins(), a.getWins()))
                .collect(Collectors.toList());
    }
}