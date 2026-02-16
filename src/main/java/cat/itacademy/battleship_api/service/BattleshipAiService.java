package cat.itacademy.battleship_api.service;

import cat.itacademy.battleship_api.model.Board;
import cat.itacademy.battleship_api.model.Ship;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class BattleshipAiService {

    private final Random random = new Random();

    public String calculateCpuTarget(Board opponentBoard) {
        List<String> shotsFired = opponentBoard.getShotsReceived();

        // 1. MODO "TARGET" (Caza y Captura de barcos heridos)
        // (Esta parte no cambia, es la prioridad máxima)
        List<String> openHits = new ArrayList<>();
        for (Ship ship : opponentBoard.getShips()) {
            if (!ship.isSunk()) {
                for (String cell : ship.getCells()) {
                    if (shotsFired.contains(cell)) {
                        openHits.add(cell);
                    }
                }
            }
        }

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

        // 2. MODO "HUNT" PRO (Búsqueda basada en el barco más pequeño vivo)
        // 🔥 CAMBIO CLAVE: Ya no miramos si queda 1 solo barco.
        // Miramos cuál es el tamaño MÍNIMO necesario para hacer daño.
        int minShipSize = getSmallestAliveShipSize(opponentBoard);

        return generateSmartGapCoordinate(shotsFired, minShipSize);
    }

    // ==========================================
    // LÓGICA DE HUECOS (SMART GAP)
    // ==========================================

    private int getSmallestAliveShipSize(Board board) {
        int minSize = 10; // Empezamos con un valor alto
        boolean anyAlive = false;

        for (Ship ship : board.getShips()) {
            if (!ship.isSunk()) {
                if (ship.getSize() < minSize) {
                    minSize = ship.getSize();
                }
                anyAlive = true;
            }
        }
        // Si no quedan barcos (raro si llega aquí), devolvemos 2 por defecto
        return anyAlive ? minSize : 2;
    }

    private String generateSmartGapCoordinate(List<String> shots, int targetSize) {
        String coord;
        int attempts = 0;

        // Intentamos encontrar una coordenada válida donde quepa el barco más pequeño
        do {
            char row = (char) ('A' + random.nextInt(10));
            int col = 1 + random.nextInt(10);
            coord = "" + row + col;
            attempts++;

            // Si tras 5000 intentos no encuentra hueco (tablero muy lleno),
            // usa el método de emergencia para no bloquear el juego.
            if (attempts > 5000) return findFirstFreeCell(shots);

        } while (shots.contains(coord) || !fitsInGap(coord, shots, targetSize));

        return coord;
    }

    /**
     * Verifica si en 'coord' cabe un barco de tamaño 'targetSize' (H o V).
     */
    private boolean fitsInGap(String coord, List<String> shots, int targetSize) {
        char row = coord.charAt(0);
        int col = Integer.parseInt(coord.substring(1));

        // 1. Espacio Horizontal
        int freeLeft = countFreeSpaces(row, col, 0, -1, shots);
        int freeRight = countFreeSpaces(row, col, 0, 1, shots);
        if ((freeLeft + 1 + freeRight) >= targetSize) return true;

        // 2. Espacio Vertical
        int freeUp = countFreeSpaces(row, col, -1, 0, shots);
        int freeDown = countFreeSpaces(row, col, 1, 0, shots);
        if ((freeUp + 1 + freeDown) >= targetSize) return true;

        return false;
    }

    private int countFreeSpaces(char startRow, int startCol, int dRow, int dCol, List<String> shots) {
        int count = 0;
        char currentRow = (char)(startRow + dRow);
        int currentCol = startCol + dCol;

        while (isValidCoordinate(currentRow, currentCol)) {
            String coord = "" + currentRow + currentCol;
            if (shots.contains(coord)) {
                break;
            }
            count++;
            currentRow = (char)(currentRow + dRow);
            currentCol += dCol;
        }
        return count;
    }

    // ==========================================
    // MÉTODOS AUXILIARES (Targeting)
    // ==========================================
    // (Estos métodos se mantienen igual que antes)

    private String getLineStrategyTarget(List<String> hits, List<String> alreadyShot) {
        hits.sort(String::compareTo);
        String first = hits.get(0);
        String second = hits.get(1);

        char row1 = first.charAt(0);
        char row2 = second.charAt(0);
        int col1 = Integer.parseInt(first.substring(1));
        int col2 = Integer.parseInt(second.substring(1));

        if (row1 == row2) {
            int minCol = 11, maxCol = 0;
            for(String h : hits) {
                int c = Integer.parseInt(h.substring(1));
                if(c < minCol) minCol = c;
                if(c > maxCol) maxCol = c;
            }
            String left = "" + row1 + (minCol - 1);
            if (isValidCoordinate(row1, minCol - 1) && !alreadyShot.contains(left)) return left;
            String right = "" + row1 + (maxCol + 1);
            if (isValidCoordinate(row1, maxCol + 1) && !alreadyShot.contains(right)) return right;
        } else if (col1 == col2) {
            char minRow = 'Z', maxRow = 'A';
            for(String h : hits) {
                char r = h.charAt(0);
                if(r < minRow) minRow = r;
                if(r > maxRow) maxRow = r;
            }
            String up = "" + (char)(minRow - 1) + col1;
            if (isValidCoordinate((char)(minRow - 1), col1) && !alreadyShot.contains(up)) return up;
            String down = "" + (char)(maxRow + 1) + col1;
            if (isValidCoordinate((char)(maxRow + 1), col1) && !alreadyShot.contains(down)) return down;
        }
        return null;
    }

    private List<String> getNeighbors(String coord) {
        List<String> neighbors = new ArrayList<>();
        char row = coord.charAt(0);
        int col = Integer.parseInt(coord.substring(1));

        if (row > 'A') neighbors.add("" + (char)(row - 1) + col);
        if (row < 'J') neighbors.add("" + (char)(row + 1) + col);
        if (col > 1)   neighbors.add("" + row + (col - 1));
        if (col < 10)  neighbors.add("" + row + (col + 1));
        Collections.shuffle(neighbors);
        return neighbors;
    }

    private boolean isValidCoordinate(char row, int col) {
        return row >= 'A' && row <= 'J' && col >= 1 && col <= 10;
    }

    private String findFirstFreeCell(List<String> shots) {
        for (char r = 'A'; r <= 'J'; r++) {
            for (int c = 1; c <= 10; c++) {
                String coord = "" + r + c;
                if (!shots.contains(coord)) return coord;
            }
        }
        return null; // Tablero lleno
    }
}