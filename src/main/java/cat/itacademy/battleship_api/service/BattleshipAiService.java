package cat.itacademy.battleship_api.service;

import cat.itacademy.battleship_api.model.Board;
import cat.itacademy.battleship_api.model.Ship;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class BattleshipAiService {

    public String calculateCpuTarget(Board opponentBoard) {
        List<String> shotsFired = opponentBoard.getShotsReceived();

        // A. Buscar aciertos abiertos (Barcos heridos)
        List<String> openHits = new ArrayList<>();
        for (Ship ship : opponentBoard.getShips()) {
            if (!ship.isSunk()) {
                for (String cell : ship.getCells()) {
                    if (shotsFired.contains(cell)) openHits.add(cell);
                }
            }
        }

        // B & C. Estrategia de Caza (Línea o Vecinos)
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

        // D. Estrategia de Huecos (Gap 3)
        String gapTarget = getGapStrategyTarget(shotsFired);
        if (gapTarget != null) {
            return gapTarget;
        }

        // E. Aleatorio inteligente
        return generateRandomCoordinate(shotsFired);
    }

    // --- MÉTODOS PRIVADOS DE AYUDA (Copiados de tu código original) ---

    private String getLineStrategyTarget(List<String> hits, List<String> alreadyShot) {
        hits.sort(String::compareTo);
        String first = hits.get(0);
        String second = hits.get(1);
        char row1 = first.charAt(0);
        char row2 = second.charAt(0);
        int col1 = Integer.parseInt(first.substring(1));
        int col2 = Integer.parseInt(second.substring(1));

        if (row1 == row2) { // Horizontal
            int minCol = 11, maxCol = 0;
            for(String h : hits) {
                int c = Integer.parseInt(h.substring(1));
                if(c < minCol) minCol = c;
                if(c > maxCol) maxCol = c;
            }
            String left = "" + row1 + (minCol - 1);
            if (minCol > 1 && !alreadyShot.contains(left)) return left;
            String right = "" + row1 + (maxCol + 1);
            if (maxCol < 10 && !alreadyShot.contains(right)) return right;
        }
        else if (col1 == col2) { // Vertical
            char minRow = 'Z', maxRow = 'A';
            for(String h : hits) {
                char r = h.charAt(0);
                if(r < minRow) minRow = r;
                if(r > maxRow) maxRow = r;
            }
            String up = "" + (char)(minRow - 1) + col1;
            if (minRow > 'A' && !alreadyShot.contains(up)) return up;
            String down = "" + (char)(maxRow + 1) + col1;
            if (maxRow < 'J' && !alreadyShot.contains(down)) return down;
        }
        return null;
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
            Collections.shuffle(neighbors);
        } catch (Exception e) {}
        return neighbors;
    }

    private String getGapStrategyTarget(List<String> shots) {
        // Escaneo Horizontal
        for (int r = 0; r < 10; r++) {
            int emptyCount = 0;
            for (int c = 0; c <= 10; c++) {
                boolean isVisited = (c == 10) || shots.contains(toCoordinate(r, c));
                if (!isVisited) {
                    emptyCount++;
                } else {
                    if (emptyCount == 3) return toCoordinate(r, c - 2);
                    emptyCount = 0;
                }
            }
        }
        // Escaneo Vertical
        for (int c = 0; c < 10; c++) {
            int emptyCount = 0;
            for (int r = 0; r <= 10; r++) {
                boolean isVisited = (r == 10) || shots.contains(toCoordinate(r, c));
                if (!isVisited) {
                    emptyCount++;
                } else {
                    if (emptyCount == 3) return toCoordinate(r - 2, c);
                    emptyCount = 0;
                }
            }
        }
        return null;
    }

    private String generateRandomCoordinate(List<String> shots) {
        String coord;
        int attempts = 0;
        do {
            char row = (char) ('A' + (int)(Math.random() * 10));
            int col = 1 + (int)(Math.random() * 10);
            coord = "" + row + col;
            attempts++;
            if (attempts > 200 && !shots.contains(coord)) return coord;
        } while (shots.contains(coord) || !canFitSmallestShip(coord, shots));
        return coord;
    }

    private boolean canFitSmallestShip(String coord, List<String> shots) {
        char row = coord.charAt(0);
        int col = Integer.parseInt(coord.substring(1));
        boolean leftFree  = isValidNeighbor(row, col - 1, shots);
        boolean rightFree = isValidNeighbor(row, col + 1, shots);
        boolean upFree    = isValidNeighbor((char)(row - 1), col, shots);
        boolean downFree  = isValidNeighbor((char)(row + 1), col, shots);
        return (leftFree || rightFree) || (upFree || downFree);
    }

    private boolean isValidNeighbor(char row, int col, List<String> shots) {
        if (row < 'A' || row > 'J') return false;
        if (col < 1 || col > 10) return false;
        return !shots.contains("" + row + col);
    }

    private String toCoordinate(int row, int col) {
        char rowChar = (char) ('A' + row);
        return rowChar + String.valueOf(col + 1);
    }
}