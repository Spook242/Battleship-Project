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

        // 1. MODO "TARGET" (Prioridad Máxima: Hundir heridos)
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
            // A. Estrategia de Línea (2+ impactos)
            if (openHits.size() >= 2) {
                String lineTarget = getLineStrategyTarget(openHits, shotsFired);
                if (lineTarget != null) return lineTarget;
            }
            // B. Estrategia de Vecinos (1 impacto)
            for (String hit : openHits) {
                List<String> neighbors = getNeighbors(hit);
                for (String neighbor : neighbors) {
                    if (!shotsFired.contains(neighbor)) return neighbor;
                }
            }
        }

        // 2. MODO "HUNT" INTELIGENTE (Búsqueda de Huecos)
        // Detectamos si queda UN SOLO barco vivo y obtenemos su tamaño.
        int lastShipSize = getLastAliveShipSize(opponentBoard);

        // Si queda un solo barco (sea de 5, 4, 3 o incluso 2), aplicamos la lógica de huecos
        if (lastShipSize > 0) {
            return generateSmartGapCoordinate(shotsFired, lastShipSize);
        }

        // 3. MODO ALEATORIO ESTÁNDAR (Si quedan varios barcos vivos)
        return generateRandomCoordinate(shotsFired);
    }

    // ==========================================
    // LÓGICA DE HUECOS DINÁMICA (Smart Gap)
    // ==========================================

    /**
     * Devuelve el tamaño del barco si solo queda uno vivo.
     * Devuelve -1 si hay más de un barco vivo o ninguno.
     */
    private int getLastAliveShipSize(Board board) {
        List<Ship> aliveShips = new ArrayList<>();
        for (Ship ship : board.getShips()) {
            if (!ship.isSunk()) {
                aliveShips.add(ship);
            }
        }
        if (aliveShips.size() == 1) {
            return aliveShips.get(0).getSize(); // Devuelve 5, 4, 3 o 2
        }
        return -1; // Hay varios barcos, usamos lógica estándar
    }

    private String generateSmartGapCoordinate(List<String> shots, int targetSize) {
        String coord;
        int attempts = 0;

        do {
            char row = (char) ('A' + random.nextInt(10));
            int col = 1 + random.nextInt(10);
            coord = "" + row + col;
            attempts++;

            // Si el mapa está muy lleno y no encontramos hueco rápido, salimos para no bloquear
            if (attempts > 3000) return findFirstFreeCell(shots);

        } while (shots.contains(coord) || !fitsInGap(coord, shots, targetSize));

        return coord;
    }

    /**
     * Verifica si en la coordenada 'coord' cabe un barco de tamaño 'targetSize'
     * ya sea horizontal o verticalmente.
     */
    private boolean fitsInGap(String coord, List<String> shots, int targetSize) {
        char row = coord.charAt(0);
        int col = Integer.parseInt(coord.substring(1));

        // 1. Mirar Horizontal
        int freeLeft  = countFreeSpaces(row, col, 0, -1, shots);
        int freeRight = countFreeSpaces(row, col, 0, 1, shots);
        // Espacio total = izquierda + actual(1) + derecha
        if ((freeLeft + 1 + freeRight) >= targetSize) return true;

        // 2. Mirar Vertical
        int freeUp   = countFreeSpaces(row, col, -1, 0, shots);
        int freeDown = countFreeSpaces(row, col, 1, 0, shots);
        // Espacio total = arriba + actual(1) + abajo
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
                break; // Chocamos con un disparo o borde
            }
            count++;
            currentRow = (char)(currentRow + dRow);
            currentCol += dCol;
        }
        return count;
    }

    // ==========================================
    // MÉTODOS ESTÁNDAR (TARGET & RANDOM)
    // ==========================================

    private String getLineStrategyTarget(List<String> hits, List<String> alreadyShot) {
        hits.sort(String::compareTo);
        String first = hits.get(0);
        String second = hits.get(1);

        char row1 = first.charAt(0);
        char row2 = second.charAt(0);
        int col1 = Integer.parseInt(first.substring(1));
        int col2 = Integer.parseInt(second.substring(1));

        if (row1 == row2) { // Estrategia Horizontal
            int minCol = 11, maxCol = 0;

            // 👇 CAMBIO: 'h' ahora es 'hitCoordinate' y 'c' es 'col'
            for (String hitCoordinate : hits) {
                int col = Integer.parseInt(hitCoordinate.substring(1));
                if (col < minCol) minCol = col;
                if (col > maxCol) maxCol = col;
            }

            String left = "" + row1 + (minCol - 1);
            if (isValidCoordinate(row1, minCol - 1) && !alreadyShot.contains(left)) return left;

            String right = "" + row1 + (maxCol + 1);
            if (isValidCoordinate(row1, maxCol + 1) && !alreadyShot.contains(right)) return right;

        } else if (col1 == col2) { // Estrategia Vertical
            char minRow = 'Z', maxRow = 'A';

            // 👇 CAMBIO: 'h' ahora es 'hitCoordinate' y 'r' es 'row'
            for (String hitCoordinate : hits) {
                char row = hitCoordinate.charAt(0);
                if (row < minRow) minRow = row;
                if (row > maxRow) maxRow = row;
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

    private String generateRandomCoordinate(List<String> shots) {
        String coord;
        int attempts = 0;
        do {
            char row = (char) ('A' + random.nextInt(10));
            int col = 1 + random.nextInt(10);
            coord = "" + row + col;
            attempts++;
            if (attempts > 500) return findFirstFreeCell(shots);
        } while (shots.contains(coord) || !isSmartRandomMove(coord, shots));
        return coord;
    }

    private boolean isSmartRandomMove(String coord, List<String> shots) {
        // Verifica si tiene al menos un vecino libre (no dispara a celdas 1x1 aisladas)
        char row = coord.charAt(0);
        int col = Integer.parseInt(coord.substring(1));
        return (isValidCoordinate(row, col-1) && !shots.contains("" + row + (col-1))) ||
                (isValidCoordinate(row, col+1) && !shots.contains("" + row + (col+1))) ||
                (isValidCoordinate((char)(row-1), col) && !shots.contains("" + (char)(row-1) + col)) ||
                (isValidCoordinate((char)(row+1), col) && !shots.contains("" + (char)(row+1) + col));
    }

    private boolean isValidCoordinate(char row, int col) {
        return row >= 'A' && row <= 'J' && col >= 1 && col <= 10;
    }

    private String findFirstFreeCell(List<String> shots) {
        for (char row = 'A'; row <= 'J'; row++) {
            for (int col = 1; col <= 10; col++) {
                String coord = "" + row + col;
                if (!shots.contains(coord)) return coord;
            }
        }
        return null;
    }
}