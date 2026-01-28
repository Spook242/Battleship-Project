package cat.itacademy.battleship_api.service;

import cat.itacademy.battleship_api.model.Board;
import cat.itacademy.battleship_api.model.Game;
import cat.itacademy.battleship_api.model.Player;
import cat.itacademy.battleship_api.model.Ship;
import cat.itacademy.battleship_api.repository.GameRepository;
import cat.itacademy.battleship_api.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;

    public Game createGame(String username) {
        Player player = playerRepository.findByUsername(username)
                .orElseGet(() -> playerRepository.save(new Player(username)));

        Game game = Game.builder()
                .playerId(player.getId())
                .status("PLAYING")
                .turn("PLAYER")
                .playerBoard(new Board())
                .cpuBoard(new Board())
                .build();

        placeShipsRandomly(game.getPlayerBoard());
        placeShipsRandomly(game.getCpuBoard());

        return gameRepository.save(game);
    }

    public Game playerMove(String gameId, String coordinate) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        if (!game.getStatus().equals("PLAYING")) {
            throw new RuntimeException("The game is not active");
        }
        if (!game.getTurn().equals("PLAYER")) {
            throw new RuntimeException("¡It's not your turn! Wait for the CPU!");
        }

        boolean hit = processShot(game.getCpuBoard(), coordinate);

        if (!hit) {
            game.setTurn("CPU");
            cpuMove(game);
        }

        checkWinner(game);

        return gameRepository.save(game);
    }

    private boolean processShot(Board board, String coordinate) {
        if (board.getShotsReceived().contains(coordinate)) {
            throw new RuntimeException("You have already shot this box");
        }

        board.getShotsReceived().add(coordinate);

        for (Ship ship : board.getShips()) {
            if (ship.getCells().contains(coordinate)) {
                ship.getHits().add(coordinate);

                if (ship.getHits().size() == ship.getSize()) {
                    ship.setSunk(true);
                }
                return true;
            }
        }
        return false;
    }

    private void checkWinner(Game game) {
        boolean allCpuSunk = game.getCpuBoard().getShips().stream().allMatch(Ship::isSunk);
        if (allCpuSunk) {
            game.setWinner("PLAYER");
            game.setStatus("FINISHED");
        }
    }

    private void placeShipsRandomly(Board board) {
        int[] shipSizes = {5, 4, 3, 3, 2};

        for (int size : shipSizes) {
            boolean placed = false;
            while (!placed) {
                placed = tryToPlaceShip(board, size);
            }
        }
    }

    private boolean tryToPlaceShip(Board board, int size) {
        Random random = new Random();
        int row = random.nextInt(10);
        int col = random.nextInt(10);
        boolean horizontal = random.nextBoolean();

        List<String> shipCells = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            int r = horizontal ? row : row + i;
            int c = horizontal ? col + i : col;

            if (r > 9 || c > 9) return false;

            String coordinate = toCoordinate(r, c);

            for (Ship s : board.getShips()) {
                if (s.getCells().contains(coordinate)) return false;
            }
            shipCells.add(coordinate);
        }

        Ship newShip = new Ship("Barco-" + size, size, shipCells, new ArrayList<>(), false);
        board.addShip(newShip);
        return true;
    }

    private String toCoordinate(int row, int col) {
        char rowChar = (char) ('A' + row);
        return rowChar + String.valueOf(col + 1);
    }

    private void cpuMove(Game game) {
        while (game.getTurn().equals("CPU") && "PLAYING".equals(game.getStatus())) {

            String target = pickRandomTarget(game.getPlayerBoard());

            boolean hit = processShot(game.getPlayerBoard(), target);

            checkWinner(game);

            if (!hit) {
                game.setTurn("PLAYER");
            }
        }
    }

    private String pickRandomTarget(Board board) {
        Random random = new Random();
        String coordinate;
        do {
            int row = random.nextInt(10);
            int col = random.nextInt(10);
            coordinate = toCoordinate(row, col);
        } while (board.getShotsReceived().contains(coordinate));

        return coordinate;
    }
}