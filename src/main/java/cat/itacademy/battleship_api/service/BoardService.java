package cat.itacademy.battleship_api.service;

import cat.itacademy.battleship_api.model.Board;
import cat.itacademy.battleship_api.model.Ship;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class BoardService {

    // 1. OPTIMIZACIÓN: Una sola instancia de Random para toda la clase
    private final Random random = new Random();

    public boolean processShot(Board board, String coordinate) {
        // 2. SEGURIDAD: Evitar procesar el mismo disparo dos veces
        if (board.getShotsReceived().contains(coordinate)) {
            // Opcional: Lanzar excepción o simplemente devolver false/true según convenga.
            // Aquí asumimos que si ya estaba disparado, no cambia el estado del juego.
            return false;
        }

        // Registramos el disparo
        board.getShotsReceived().add(coordinate);

        // Comprobamos si impacta en algún barco
        for (Ship ship : board.getShips()) {
            if (ship.getCells().contains(coordinate)) {
                ship.getHits().add(coordinate);

                // Verificar hundimiento
                if (ship.getHits().size() == ship.getSize()) {
                    ship.setSunk(true);
                }
                return true; // IMPACTO 💥
            }
        }
        return false; // AGUA 💧
    }

    public void placeShipsRandomly(Board board) {
        // Limpiamos el tablero por si acaso venía sucio (reinicio de partida)
        board.getShips().clear();
        board.getShotsReceived().clear();

        int[] shipSizes = {5, 4, 3, 3, 2};

        for (int size : shipSizes) {
            boolean placed = false;
            // Bucle de intentos: Sigue intentando hasta que el barco quepa sin chocar
            while (!placed) {
                placed = tryToPlaceShip(board, size);
            }
        }
    }

    // ==========================================
    // MÉTODOS PRIVADOS
    // ==========================================

    private boolean tryToPlaceShip(Board board, int size) {
        // Usamos la variable de clase 'random'
        boolean horizontal = random.nextBoolean();

        // 👇 MEJORA: Aclaramos que este es el punto de INICIO
        int startRow = random.nextInt(10); // 0-9
        int startCol = random.nextInt(10); // 0-9

        List<String> shipCells = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            // 👇 MEJORA: 'currentRow' y 'currentCol' son mucho más legibles que 'r' y 'c'
            int currentRow = horizontal ? startRow : startRow + i;
            int currentCol = horizontal ? startCol + i : startCol;

            // 1. Validar límites del tablero (0-9)
            if (currentRow > 9 || currentCol > 9) return false;

            String coordinate = toCoordinate(currentRow, currentCol);

            // 2. Validar colisión con otros barcos
            if (isOccupied(board, coordinate)) return false;

            shipCells.add(coordinate);
        }


        // Si llegamos aquí, es válido. Creamos y añadimos el barco.
        // Nota: Asegúrate de usar la lista mutable de Lombok
        Ship newShip = Ship.builder()
                .type("Ship-" + size)
                .size(size)
                .cells(shipCells)
                .hits(new ArrayList<>())
                .sunk(false)
                .build();

        board.getShips().add(newShip);
        return true;
    }

    // Método auxiliar para limpiar la lógica de colisión
    private boolean isOccupied(Board board, String coordinate) {
        for (Ship ship : board.getShips()) {
            if (ship.getCells().contains(coordinate)) return true;
        }
        return false;
    }

    private String toCoordinate(int row, int col) {
        char rowChar = (char) ('A' + row);
        return rowChar + String.valueOf(col + 1);
    }
}