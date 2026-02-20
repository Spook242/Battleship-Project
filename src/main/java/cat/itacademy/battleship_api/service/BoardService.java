package cat.itacademy.battleship_api.service;

import cat.itacademy.battleship_api.exception.InvalidMoveException; // Asegúrate de importar tu excepción
import cat.itacademy.battleship_api.model.Board;
import cat.itacademy.battleship_api.model.Ship;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class BoardService {

    private final Random random = new Random();

    // ==========================================
    // 💥 PROCESAR DISPARO
    // ==========================================
    public boolean processShot(Board board, String coordinate) {

        // 1. SEGURIDAD: Evitar perder el turno por disparar al mismo sitio
        if (board.getShotsReceived().contains(coordinate)) {
            // Lanzamos error en vez de devolver 'false', así el Front avisa al jugador
            // y el GameService NO cambia el turno a la CPU.
            throw new InvalidMoveException("Ya has disparado a la coordenada " + coordinate);
        }

        // 2. Usamos la "inteligencia" del tablero
        board.receiveShot(coordinate);

        // 3. Usamos la "inteligencia" del barco (¡Mira qué limpio queda esto!)
        for (Ship ship : board.getShips()) {
            if (ship.receiveHit(coordinate)) {
                return true; // 💥 IMPACTO (El barco ya calcula solo si se ha hundido)
            }
        }

        return false; // 💧 AGUA
    }

    // ==========================================
    // 🚢 COLOCAR BARCOS
    // ==========================================
    public void placeShipsRandomly(Board board) {
        board.getShips().clear();
        board.getShotsReceived().clear();

        int[] shipSizes = {5, 4, 3, 3, 2};

        for (int size : shipSizes) {
            boolean placed = false;
            while (!placed) {
                placed = tryToPlaceShip(board, size);
            }
        }
    }

    // ==========================================
    // 🛠️ MÉTODOS PRIVADOS
    // ==========================================
    private boolean tryToPlaceShip(Board board, int size) {
        boolean horizontal = random.nextBoolean();
        int startRow = random.nextInt(10);
        int startCol = random.nextInt(10);

        List<String> shipCells = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            int currentRow = horizontal ? startRow : startRow + i;
            int currentCol = horizontal ? startCol + i : startCol;

            if (currentRow > 9 || currentCol > 9) return false;

            String coordinate = toCoordinate(currentRow, currentCol);

            if (isOccupied(board, coordinate)) return false;

            shipCells.add(coordinate);
        }

        // 4. MEJORA: Builder más limpio.
        // Como 'hits' y 'sunk' tienen @Builder.Default en el modelo, no hace falta ponerlos aquí.
        Ship newShip = Ship.builder()
                .type("Ship-" + size)
                .size(size)
                .cells(shipCells)
                .build();

        board.addShip(newShip); // Usamos el método limpio que creamos en Board
        return true;
    }

    private boolean isOccupied(Board board, String coordinate) {
        // 5. MEJORA PRO: Usamos Streams para que sea más directo
        return board.getShips().stream()
                .anyMatch(ship -> ship.getCells().contains(coordinate));
    }

    private String toCoordinate(int row, int col) {
        char rowChar = (char) ('A' + row);
        return rowChar + String.valueOf(col + 1);
    }
}