package cat.itacademy.battleship_api.service;

import cat.itacademy.battleship_api.dto.GameStartResponse;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final JwtService jwtService;

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
                // .cpuPendingTargets(...) <- YA NO HACE FALTA USAR ESTO, LA IA LO CALCULA AL VUELO
                .build();

        //placeShipsRandomly(game.getPlayerBoard());
        placeShipsRandomly(game.getCpuBoard());

        return gameRepository.save(game);
    }

    // Inyecta JwtService en el constructor de GameService


    public GameStartResponse startNewGame(String username) {
        // 1. Validar (Lanza excepción si falla, capturada por GlobalExceptionHandler)
        if (username == null || username.isBlank()) {
            throw new InvalidGameActionException("Username is required");
        }

        // 2. Crear Juego
        Game game = createGame(username); // Tu método existente

        // 3. Generar Token (Ahora es responsabilidad del servicio orquestar esto)
        String token = jwtService.generateToken(
                Map.of("gameId", game.getId()),
                username
        );

        // 4. Devolver el paquete completo
        return new GameStartResponse(game, token);
    }

    public Game startBattle(String gameId, List<Ship> playerShips) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        // Validar que sean 5 barcos (opcional pero recomendado)
        if (playerShips.size() != 5) {
            throw new RuntimeException("You must place all 5 ships!");
        }

        // Guardamos los barcos en el tablero del jugador
        game.getPlayerBoard().setShips(playerShips);

        // Cambiamos el estado a JUGANDO
        game.setStatus("PLAYING");

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
        List<String> shotsFired = opponentBoard.getShotsReceived();

        // ... (Aquí va tu lógica A, B y C para barcos heridos que ya tenías) ...
        // A. Buscar aciertos abiertos
        List<String> openHits = new ArrayList<>();
        for (Ship ship : opponentBoard.getShips()) {
            if (!ship.isSunk()) {
                for (String cell : ship.getCells()) {
                    if (shotsFired.contains(cell)) openHits.add(cell);
                }
            }
        }

        // Si hay barcos heridos, usamos la lógica de caza (Línea o Vecinos)
        if (!openHits.isEmpty()) {
            if (openHits.size() >= 2) {
                String lineTarget = getLineStrategyTarget(openHits, shotsFired);
                if (lineTarget != null) return lineTarget;
            }
            for (String hit : openHits) {
                List<String> neighbors = getNeighbors(hit);
                for (String neighbor : neighbors) {
                    if (!shotsFired.contains(neighbor)) return neighbor;
                }
            }
        }

        // =======================================================
        // D. AQUÍ LA NUEVA LÓGICA: Si no estamos cazando, BUSCAMOS HUECOS
        // =======================================================
        String gapTarget = getGapStrategyTarget(shotsFired);
        if (gapTarget != null) {
            System.out.println("🤖 CPU: Detectado hueco de 3. Disparando al centro: " + gapTarget);
            return gapTarget;
        }

        // E. Si no hay huecos de 3, usamos el aleatorio inteligente (paridad) que hicimos antes
        return generateRandomCoordinate(shotsFired);
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

    // ==========================================
    // MÉTODO GENERAR ALEATORIO (MEJORADO)
    // ==========================================
    private String generateRandomCoordinate(List<String> shots) {
        String coord;
        int attempts = 0;

        do {
            char row = (char) ('A' + (int)(Math.random() * 10));
            int col = 1 + (int)(Math.random() * 10);
            coord = "" + row + col;
            attempts++;

            // Si llevamos muchos intentos (ej. final de partida), relajamos la restricción
            // para evitar bucles infinitos.
            if (attempts > 200) {
                if (!shots.contains(coord)) return coord;
            }

        } while (shots.contains(coord) || !canFitSmallestShip(coord, shots));

        return coord;
    }

    /**
     * Comprueba si en la coordenada 'coord' cabe al menos un barco de tamaño 2.
     * Revisa si tiene algún vecino (Arriba, Abajo, Izq, Der) LIBRE (no disparado).
     * Si está rodeada de disparos o bordes, devuelve false.
     */
    private boolean canFitSmallestShip(String coord, List<String> shots) {
        char row = coord.charAt(0);
        int col = Integer.parseInt(coord.substring(1));

        // 1. Mirar Horizonte (Izquierda o Derecha libres)
        boolean leftFree  = isValidNeighbor(row, col - 1, shots);
        boolean rightFree = isValidNeighbor(row, col + 1, shots);
        boolean fitsHorizontally = leftFree || rightFree;

        // 2. Mirar Vertical (Arriba o Abajo libres)
        boolean upFree    = isValidNeighbor((char)(row - 1), col, shots);
        boolean downFree  = isValidNeighbor((char)(row + 1), col, shots);
        boolean fitsVertically = upFree || downFree;

        // Si cabe en horizontal O en vertical, es un tiro válido.
        return fitsHorizontally || fitsVertically;
    }

    // Helper para verificar si un vecino es válido (dentro del mapa y no disparado)
    private boolean isValidNeighbor(char row, int col, List<String> shots) {
        if (row < 'A' || row > 'J') return false; // Fuera del mapa
        if (col < 1 || col > 10) return false;    // Fuera del mapa

        String neighbor = "" + row + col;
        return !shots.contains(neighbor); // Si ya se disparó ahí, cuenta como "muro"
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

    // ==========================================
    // ESTRATEGIA: FRANCOTIRADOR DE HUECOS (GAP 3)
    // ==========================================
    // Busca secuencias de exactamente 3 casillas desconocidas y devuelve la del medio.
    // Ejemplo: Si B7, C7, D7 están vacías (y rodeadas de disparos), devuelve C7.
    private String getGapStrategyTarget(List<String> shots) {

        // 1. ESCANEO HORIZONTAL (Barrido por filas A-J)
        for (int r = 0; r < 10; r++) {
            int emptyCount = 0;
            for (int c = 0; c <= 10; c++) { // <= 10 para simular el borde derecho
                // Si llegamos a c=10 (borde) o la casilla ya fue disparada, es un "muro"
                boolean isVisited = (c == 10) || shots.contains(toCoordinate(r, c));

                if (!isVisited) {
                    emptyCount++;
                } else {
                    // Si encontramos un muro y veníamos de 3 vacíos exactos
                    if (emptyCount == 3) {
                        // El hueco está en los índices [c-3, c-2, c-1]
                        // El medio es c-2
                        return toCoordinate(r, c - 2);
                    }
                    emptyCount = 0; // Reiniciamos contador
                }
            }
        }

        // 2. ESCANEO VERTICAL (Barrido por columnas 1-10)
        for (int c = 0; c < 10; c++) {
            int emptyCount = 0;
            for (int r = 0; r <= 10; r++) { // <= 10 para simular el borde inferior
                // Si llegamos a r=10 (borde) o la casilla ya fue disparada
                boolean isVisited = (r == 10) || shots.contains(toCoordinate(r, c));

                if (!isVisited) {
                    emptyCount++;
                } else {
                    if (emptyCount == 3) {
                        // El hueco está en las filas [r-3, r-2, r-1]
                        // El medio es r-2
                        return toCoordinate(r - 2, c);
                    }
                    emptyCount = 0;
                }
            }
        }

        return null; // No se encontraron huecos perfectos de 3
    }
}
