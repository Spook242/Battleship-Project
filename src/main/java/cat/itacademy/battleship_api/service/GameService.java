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
import cat.itacademy.battleship_api.model.enums.GameStatus;
import cat.itacademy.battleship_api.model.enums.PlayerTurn;
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

    public GameStartResponse startNewGame(String username) {
        if (username == null || username.isBlank()) {
            throw new InvalidGameActionException("Username is required");
        }

        Player player = playerRepository.findByUsername(username)
                .orElseGet(() -> playerRepository.save(Player.builder().username(username).build()));

        Game game = Game.builder()
                .playerId(player.getId())
                .status(GameStatus.SETUP) // <-- CORREGIDO PARA EL FRONTEND
                .turn(PlayerTurn.PLAYER)
                .playerBoard(new Board())
                .cpuBoard(new Board())
                .build();

        boardService.placeShipsRandomly(game.getCpuBoard());
        gameRepository.save(game);
        String token = jwtService.generateToken(Map.of("gameId", game.getId()), username);

        return GameStartResponse.builder().game(game).token(token).build();
    }

    public Game startBattle(String gameId, List<Ship> playerShips) {
        Game game = findGameOrThrow(gameId);

        if (game.getStatus() != GameStatus.SETUP) { // <-- CORREGIDO
            throw new InvalidGameActionException("Game is not in SETUP mode");
        }

        if (playerShips == null || playerShips.size() != 5 || playerShips.stream().anyMatch(s -> s.getCells().isEmpty())) {
            throw new InvalidGameActionException("You must place exactly 5 valid ships!");
        }

        game.getPlayerBoard().setShips(playerShips);
        game.setStatus(GameStatus.PLAYING); // <-- CORREGIDO PARA EL FRONTEND

        return gameRepository.save(game);
    }

    public Game playerMove(String gameId, String coordinate) {
        Game game = findGameOrThrow(gameId);
        validateTurn(game, PlayerTurn.PLAYER);
        boolean hit = boardService.processShot(game.getCpuBoard(), coordinate);

        if (checkWinner(game)) {
            return gameRepository.save(game);
        }

        if (!hit) {
            game.setTurn(PlayerTurn.CPU);
        }

        return gameRepository.save(game);
    }

    public Game playCpuTurn(String gameId) {
        return gameRepository.findById(gameId)
                .filter(g -> g.getStatus() == GameStatus.PLAYING && g.getTurn() == PlayerTurn.CPU) // <-- CORREGIDO
                .map(game -> {
                    String target = aiService.calculateCpuTarget(game.getPlayerBoard());
                    boolean hit = boardService.processShot(game.getPlayerBoard(), target);

                    if (checkWinner(game)) {
                        return gameRepository.save(game);
                    }

                    if (!hit) {
                        game.setTurn(PlayerTurn.PLAYER);
                    }
                    return gameRepository.save(game);
                })
                .orElseThrow(() -> new InvalidGameActionException("Cannot play CPU turn"));
    }

    private boolean checkWinner(Game game) {
        boolean cpuDefeated = game.getCpuBoard().getShips().stream().allMatch(Ship::isSunk);
        if (cpuDefeated) {
            game.setWinner("PLAYER");
            game.setStatus(GameStatus.FINISHED);
            return true;
        }

        boolean playerDefeated = game.getPlayerBoard().getShips().stream().allMatch(Ship::isSunk);
        if (playerDefeated) {
            game.setWinner("CPU");
            game.setStatus(GameStatus.FINISHED);
            return true;
        }
        return false;
    }

    public List<PlayerScoreDTO> getRanking() {
        return playerRepository.findAll().stream()
                .map(player -> {
                    long wins = gameRepository.countByPlayerIdAndWinner(player.getId(), "PLAYER");
                    return PlayerScoreDTO.builder().username(player.getUsername()).wins(wins).build();
                })
                .filter(dto -> dto.getWins() > 0)
                .sorted(Comparator.comparingLong(PlayerScoreDTO::getWins).reversed())
                .collect(Collectors.toList());
    }

    private Game findGameOrThrow(String gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found with ID: " + gameId));
    }

    private void validateTurn(Game game, PlayerTurn expectedTurn) {
        if (game.getStatus() != GameStatus.PLAYING) { // <-- CORREGIDO
            throw new InvalidGameActionException("Game is finished or not started.");
        }
        if (game.getTurn() != expectedTurn) {
            throw new InvalidMoveException("It is not " + expectedTurn + "'s turn.");
        }
    }
}