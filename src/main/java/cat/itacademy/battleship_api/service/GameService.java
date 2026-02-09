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
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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
                // .cpuPendingTargets(...) <- YA NO HACE FALTA USAR ESTO, LA IA LO CALCULA AL VUELO
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
            throw new RuntimeException("It's not your turn! Wait for the CPU!");
        }

        boolean hit = processShot(game.getCpuBoard(), coordinate);

        if (!hit) {
            game.setTurn("CPU");
        }

        checkWinner(game);

        return gameRepository.save(game);
    }

    // ==========================================
    // 3. TURNO DE LA CPU (CORREGIDO)
    // ==========================================
    public Game playCpuTurn(String gameId) {
        Game game = gameRepository.findById(gameId).orElse(null);
        if (game == null) return null;

        // 1. PEDIMOS AL CEREBRO QUE CALCULE EL MEJOR DISPARO
        // (Ya incluye lógica de línea, vecinos o aleatorio)
        String targetCoordinate = calculateCpuTarget(game.getPlayerBoard());

        // 2. EJECUTAMOS EL DISPARO
        boolean isHit = processShot(game.getPlayerBoard(), targetCoordinate);

        // 3. GESTIONAR TURNO
        if (!isHit) {
            game.setTurn("PLAYER");
        }
        // Si acierta (isHit), la CPU mantiene el turno (no cambiamos a PLAYER)
        // y en la siguiente llamada volverá a calcular targets basándose en el acierto.

        checkWinner(game);

        return gameRepository.save(game);
    }

    // ... otros métodos ...

    // ==========================================
    // 8. RANKING
    // ==========================================
    public List<cat.itacademy.battleship_api.dto.PlayerScoreDTO> getRanking() {
        return playerRepository.findAll().stream()
                .map(player -> {
                    long wins = gameRepository.countByPlayerIdAndWinner(player.getId(), "PLAYER");
                    return new cat.itacademy.battleship_api.dto.PlayerScoreDTO(player.getUsername(), wins);
                })
                .filter(score -> score.getWins() > 0) // Opcional: Mostrar solo ganadores
                .sorted((a, b) -> Long.compare(b.getWins(), a.getWins())) // Ordenar Mayor a Menor
                .collect(Collectors.toList());
    }

    // ==========================================
    // 4. LÓGICA DE DISPARO (COMÚN)
    // ==========================================
    private boolean processShot(Board board, String coordinate) {
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

    // ==========================================
    // 6. IA: CEREBRO (LÓGICA NUEVA)
    // ==========================================

    private String calculateCpuTarget(Board opponentBoard) {
        // A. Buscar aciertos en barcos que NO se han hundido todavía ("Heridos")
        List<String> openHits = new ArrayList<>();

        for (Ship ship : opponentBoard.getShips()) {
            if (!ship.isSunk()) {
                for (String cell : ship.getCells()) {
                    // Si la celda fue disparada, es un "Hit abierto"
                    if (opponentBoard.getShotsReceived().contains(cell)) {
                        openHits.add(cell);
                    }
                }
            }
        }

        // B. ESTRATEGIA DE LÍNEA: Si hay 2 o más aciertos en el mismo barco vivo
        if (openHits.size() >= 2) {
            String lineTarget = getLineStrategyTarget(openHits, opponentBoard.getShotsReceived());
            if (lineTarget != null) return lineTarget;
        }

        // C. ESTRATEGIA DE VECINOS: Si hay aciertos pero no formamos línea aún
        if (!openHits.isEmpty()) {
            // Probamos vecinos de todos los aciertos encontrados
            for (String hit : openHits) {
                List<String> neighbors = getNeighbors(hit);
                for (String neighbor : neighbors) {
                    if (!opponentBoard.getShotsReceived().contains(neighbor)) {
                        return neighbor; // Disparar a un vecino válido
                    }
                }
            }
        }

        // D. MODO CAZA: Aleatorio puro
        return generateRandomCoordinate(opponentBoard.getShotsReceived());
    }

    // LÓGICA DE LÍNEA (Horizontal/Vertical)
    private String getLineStrategyTarget(List<String> hits, List<String> alreadyShot) {
        hits.sort(String::compareTo); // Ordenar coordenadas

        String first = hits.get(0);
        String second = hits.get(1);

        char row1 = first.charAt(0);
        char row2 = second.charAt(0);

        // Extraer columnas (Ojo con el substring para números de 2 dígitos como '10')
        int col1 = Integer.parseInt(first.substring(1));
        int col2 = Integer.parseInt(second.substring(1));

        // 1. MISMA FILA (Horizontal) -> A4, A5
        if (row1 == row2) {
            int minCol = 11, maxCol = 0;
            // Buscar los extremos reales de todos los impactos de este barco
            for(String h : hits) {
                int c = Integer.parseInt(h.substring(1));
                if(c < minCol) minCol = c;
                if(c > maxCol) maxCol = c;
            }

            // Probar Izquierda
            String left = "" + row1 + (minCol - 1);
            if (minCol > 1 && !alreadyShot.contains(left)) return left;

            // Probar Derecha
            String right = "" + row1 + (maxCol + 1);
            if (maxCol < 10 && !alreadyShot.contains(right)) return right;
        }

        // 2. MISMA COLUMNA (Vertical) -> B4, C4
        else if (col1 == col2) {
            char minRow = 'Z', maxRow = 'A';
            for(String h : hits) {
                char r = h.charAt(0);
                if(r < minRow) minRow = r;
                if(r > maxRow) maxRow = r;
            }

            // Probar Arriba
            String up = "" + (char)(minRow - 1) + col1;
            if (minRow > 'A' && !alreadyShot.contains(up)) return up;

            // Probar Abajo
            String down = "" + (char)(maxRow + 1) + col1;
            if (maxRow < 'J' && !alreadyShot.contains(down)) return down;
        }

        return null; // Línea bloqueada o confusa, volver a vecinos
    }

    private List<String> getNeighbors(String coord) {
        List<String> neighbors = new ArrayList<>();
        try {
            char row = coord.charAt(0);
            int col = Integer.parseInt(coord.substring(1));

            if (row > 'A') neighbors.add("" + (char)(row - 1) + col);
            if (row < 'J') neighbors.add("" + (char)(row + 1) + col);
            if (col > 1) neighbors.add("" + row + (col - 1));
            if (col < 10) neighbors.add("" + row + (col + 1));

            Collections.shuffle(neighbors); // Aleatorizar para no ser predecible
        } catch (Exception e) {}
        return neighbors;
    }

    private String generateRandomCoordinate(List<String> shots) {
        String coord;
        do {
            char row = (char) ('A' + (int)(Math.random() * 10));
            int col = 1 + (int)(Math.random() * 10);
            coord = "" + row + col;
        } while (shots.contains(coord));
        return coord;
    }

    // ==========================================
    // 7. SETUP DE BARCOS
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
}