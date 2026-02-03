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

    // ==========================================
    // 1. CREAR JUEGO
    // ==========================================
    public Game createGame(String username) {
        Player player = playerRepository.findByUsername(username)
                .orElseGet(() -> playerRepository.save(new Player(username)));

        Game game = Game.builder()
                .playerId(player.getId())
                .status("PLAYING")
                .turn("PLAYER")
                .playerBoard(new Board())
                .cpuBoard(new Board())
                .cpuPendingTargets(new ArrayList<>()) // Inicializamos la lista vacía
                .build();

        placeShipsRandomly(game.getPlayerBoard());
        placeShipsRandomly(game.getCpuBoard());

        return gameRepository.save(game);
    }

    // ==========================================
    // 2. TURNO DEL JUGADOR
    // ==========================================
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

        // Si fallamos, cambio de turno. Si acertamos, seguimos tirando (opcional, aquí he puesto que cambia si falla)
        if (!hit) {
            game.setTurn("CPU");
        }

        checkWinner(game); // Comprobar si el jugador ganó

        return gameRepository.save(game);
    }

    // ==========================================
    // 3. TURNO DE LA CPU (INTELIGENTE)
    // ==========================================
    public Game playCpuTurn(String gameId) {
        Game game = gameRepository.findById(gameId).orElse(null);
        if (game == null) return null;

        String targetCoordinate = null;

        // A. MODO CAZA: Buscar objetivos pendientes
        while (!game.getCpuPendingTargets().isEmpty()) {
            String candidate = game.getCpuPendingTargets().remove(0);
            if (!game.getPlayerBoard().getShotsReceived().contains(candidate)) {
                targetCoordinate = candidate;
                break;
            }
        }

        // B. MODO ALEATORIO: Si no hay objetivos, disparo al azar
        if (targetCoordinate == null) {
            do {
                targetCoordinate = getRandomCoordinate();
            } while (game.getPlayerBoard().getShotsReceived().contains(targetCoordinate));
        }

        // C. EJECUTAR EL DISPARO
        boolean isHit = processShot(game.getPlayerBoard(), targetCoordinate);
        // Nota: Reutilizo processShot que ya tenías, hace lo mismo que checkHit y guarda el hit

        // D. SI ACIERTA -> AÑADIR VECINOS
        if (isHit) {
            List<String> neighbors = getNeighbors(targetCoordinate);
            for (String neighbor : neighbors) {
                if (!game.getPlayerBoard().getShotsReceived().contains(neighbor)
                        && !game.getCpuPendingTargets().contains(neighbor)) {
                    game.getCpuPendingTargets().add(neighbor);
                }
            }
        } else {
            // SI FALLA -> Turno del jugador
            game.setTurn("PLAYER");
        }

        checkWinner(game); // Comprobar si la CPU ganó

        return gameRepository.save(game);
    }

    // ==========================================
    // 4. LÓGICA DE DISPARO (COMÚN)
    // ==========================================
    private boolean processShot(Board board, String coordinate) {
        // En la CPU no lanzamos excepción si repite, solo en el player.
        // Pero como la lógica de CPU ya evita repetir, esto está bien.

        board.getShotsReceived().add(coordinate);

        for (Ship ship : board.getShips()) {
            if (ship.getCells().contains(coordinate)) {
                ship.getHits().add(coordinate);
                if (ship.getHits().size() == ship.getSize()) {
                    ship.setSunk(true);
                }
                return true; // Impacto
            }
        }
        return false; // Agua
    }

    // ==========================================
    // 5. COMPROBAR GANADOR
    // ==========================================
    private void checkWinner(Game game) {
        // 1. ¿Ganó el Player? (Todos los barcos de la CPU hundidos)
        boolean allCpuSunk = game.getCpuBoard().getShips().stream().allMatch(Ship::isSunk);
        if (allCpuSunk) {
            game.setWinner("PLAYER");
            game.setStatus("FINISHED");
            return;
        }

        // 2. ¿Ganó la CPU? (Todos los barcos del Player hundidos)
        boolean allPlayerSunk = game.getPlayerBoard().getShips().stream().allMatch(Ship::isSunk);
        if (allPlayerSunk) {
            game.setWinner("CPU");
            game.setStatus("FINISHED");
        }
    }

    // ==========================================
    // 6. MÉTODOS AUXILIARES DE LA IA
    // ==========================================

    // Obtener vecinos válidos (Arriba, Abajo, Izq, Der)
    private List<String> getNeighbors(String coord) {
        List<String> neighbors = new ArrayList<>();
        try {
            char row = coord.charAt(0);
            int col = Integer.parseInt(coord.substring(1));

            // ARRIBA
            if (row > 'A') neighbors.add("" + (char)(row - 1) + col);
            // ABAJO
            if (row < 'J') neighbors.add("" + (char)(row + 1) + col);
            // IZQUIERDA
            if (col > 1) neighbors.add("" + row + (col - 1));
            // DERECHA
            if (col < 10) neighbors.add("" + row + (col + 1));
        } catch (Exception e) {
            // Ignorar error de parseo
        }
        return neighbors;
    }

    private String getRandomCoordinate() {
        Random random = new Random();
        char row = (char) ('A' + random.nextInt(10));
        int col = 1 + random.nextInt(10);
        return "" + row + col;
    }

    // ==========================================
    // 7. COLOCACIÓN DE BARCOS (SETUP)
    // ==========================================
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
            int c = horizontal ? col + i : col; // El array va de 0-9, la lógica de coords de 1-10

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
}