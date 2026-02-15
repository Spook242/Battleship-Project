package cat.itacademy.battleship_api.service;

import cat.itacademy.battleship_api.dto.GameStartResponse;
import cat.itacademy.battleship_api.dto.PlayerScoreDTO;
import cat.itacademy.battleship_api.exception.GameNotFoundException;
import cat.itacademy.battleship_api.exception.InvalidGameActionException;
import cat.itacademy.battleship_api.exception.InvalidMoveException;
import cat.itacademy.battleship_api.model.Board;
import cat.itacademy.battleship_api.model.Game;
import cat.itacademy.battleship_api.model.Player;
import cat.itacademy.battleship_api.model.Ship;
import cat.itacademy.battleship_api.repository.GameRepository;
import cat.itacademy.battleship_api.repository.PlayerRepository;
import cat.itacademy.battleship_api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final JwtService jwtService;
    private final BattleshipAiService aiService;
    private final BoardService boardService;

    // ==========================================
    // 1. CREAR JUEGO
    // ==========================================
    public GameStartResponse startNewGame(String username) {
        if (username == null || username.isBlank()) {
            throw new InvalidGameActionException("Username is required");
        }

        Player player = playerRepository.findByUsername(username)
                .orElseGet(() -> playerRepository.save(new Player(username)));

        Game game = Game.builder()
                .playerId(player.getId())
                .status("SETUP")
                .turn("PLAYER")
                .playerBoard(new Board())
                .cpuBoard(new Board())
                .build();

        boardService.placeShipsRandomly(game.getCpuBoard());

        gameRepository.save(game); // Mongo genera el ID String aquí

        String token = jwtService.generateToken(Map.of("gameId", game.getId()), username);

        return new GameStartResponse(game, token);
    }

    // ==========================================
    // 2. INICIAR BATALLA
    // ==========================================
    public Game startBattle(String gameId, List<Ship> playerShips) {
        Game game = findGameOrThrow(gameId); // Pasamos String directo

        if (!"SETUP".equals(game.getStatus())) {
            throw new InvalidGameActionException("Game is not in SETUP mode");
        }

        if (playerShips == null || playerShips.size() != 5 || playerShips.stream().anyMatch(s -> s.getCells().isEmpty())) {
            throw new InvalidGameActionException("You must place exactly 5 valid ships!");
        }

        game.getPlayerBoard().setShips(playerShips);
        game.setStatus("PLAYING");

        return gameRepository.save(game);
    }

    // ==========================================
    // 3. TURNO DEL JUGADOR
    // ==========================================
    public Game playerMove(String gameId, String coordinate) {
        Game game = findGameOrThrow(gameId); // Pasamos String directo

        validateTurn(game, "PLAYER");

        boolean hit = boardService.processShot(game.getCpuBoard(), coordinate);

        if (checkWinner(game)) {
            return gameRepository.save(game);
        }

        if (!hit) {
            game.setTurn("CPU");
        }

        return gameRepository.save(game);
    }

    // ==========================================
    // 4. TURNO DE LA CPU
    // ==========================================
    public Game playCpuTurn(String gameId) {
        // 🟢 CORRECCIÓN: Usamos el String directamente. Nada de Long.parse...
        return gameRepository.findById(gameId)
                .filter(g -> "PLAYING".equals(g.getStatus()) && "CPU".equals(g.getTurn()))
                .map(game -> {
                    String target = aiService.calculateCpuTarget(game.getPlayerBoard());
                    boolean hit = boardService.processShot(game.getPlayerBoard(), target);

                    if (checkWinner(game)) {
                        return gameRepository.save(game);
                    }

                    if (!hit) {
                        game.setTurn("PLAYER");
                    }
                    return gameRepository.save(game);
                })
                .orElseThrow(() -> new InvalidGameActionException("Cannot play CPU turn: Game not found or not CPU turn"));
    }

    // ==========================================
    // LÓGICA DE VICTORIA
    // ==========================================
    private boolean checkWinner(Game game) {
        boolean cpuDefeated = game.getCpuBoard().getShips().stream()
                .allMatch(Ship::isSunk);

        if (cpuDefeated) {
            game.setWinner("PLAYER");
            game.setStatus("FINISHED");
            return true;
        }

        boolean playerDefeated = game.getPlayerBoard().getShips().stream()
                .allMatch(Ship::isSunk);

        if (playerDefeated) {
            game.setWinner("CPU");
            game.setStatus("FINISHED");
            return true;
        }

        return false;
    }

    // ==========================================
    // RANKING
    // ==========================================
    public List<PlayerScoreDTO> getRanking() {
        return playerRepository.findAll().stream()
                .map(player -> {
                    // Aquí asumimos que PlayerId sigue siendo Long (según tu modelo Player).
                    // Si Player también es String, asegúrate de que el repositorio lo soporte.
                    long wins = gameRepository.countByPlayerIdAndWinner(player.getId(), "PLAYER");
                    return new PlayerScoreDTO(player.getUsername(), wins);
                })
                .filter(dto -> dto.getWins() > 0)
                .sorted(Comparator.comparingLong(PlayerScoreDTO::getWins).reversed())
                .collect(Collectors.toList());
    }

    // ==========================================
    // MÉTODOS AUXILIARES
    // ==========================================

    // 🟢 CORRECCIÓN: Eliminada conversión a Long. Se usa String directo.
    private Game findGameOrThrow(String gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found with ID: " + gameId));
    }

    private void validateTurn(Game game, String expectedTurn) {
        if (!"PLAYING".equals(game.getStatus())) {
            throw new InvalidGameActionException("Game is finished or not started.");
        }
        if (!expectedTurn.equals(game.getTurn())) {
            throw new InvalidMoveException("It is not " + expectedTurn + "'s turn.");
        }
    }
}