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

        // 1. MODO CAZA: Buscar barcos heridos (tocados pero no hundidos)
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

        // 2. ESTRATEGIA: Si hay heridos...
        if (!openHits.isEmpty()) {
            // A. Si hay 2 o más impactos en un barco, deducir la línea (Horizontal/Vertical)
            if (openHits.size() >= 2) {
                String lineTarget = getLineStrategyTarget(openHits, shotsFired);
                if (lineTarget != null) return lineTarget;
            }

            // B. Si solo hay 1 impacto, disparar a los vecinos (Arriba, Abajo, Izq, Der)
            for (String hit : openHits) {
                List<String> neighbors = getNeighbors(hit);
                for (String neighbor : neighbors) {
                    if (!shotsFired.contains(neighbor)) return neighbor;
                }
            }
        }

        // 3. MODO ALEATORIO: Disparar al azar (buscando huecos lógicos)
        return generateRandomCoordinate(shotsFired);
    }

    // ==========================================
    // MÉTODOS PRIVADOS DE LÓGICA
    // ==========================================

    /**
     * Si tenemos A4 y A5 tocados, intentamos disparar a A3 o A6.
     */
    private String getLineStrategyTarget(List<String> hits, List<String> alreadyShot) {
        hits.sort(String::compareTo); // Ordenar coordenadas

        // Tomamos dos puntos de referencia para ver la dirección
        String first = hits.get(0);
        String second = hits.get(1);

        char row1 = first.charAt(0);
        char row2 = second.charAt(0);
        // Parsear columna (teniendo en cuenta que puede ser '10')
        int col1 = Integer.parseInt(first.substring(1));
        int col2 = Integer.parseInt(second.substring(1));

        if (row1 == row2) {
            // --- HORIZONTAL ---
            int minCol = 11, maxCol = 0;
            // Buscamos los extremos de la línea actual de impactos
            for(String h : hits) {
                int c = Integer.parseInt(h.substring(1));
                if(c < minCol) minCol = c;
                if(c > maxCol) maxCol = c;
            }

            // Probar izquierda
            String left = "" + row1 + (minCol - 1);
            if (isValidCoordinate(row1, minCol - 1) && !alreadyShot.contains(left)) return left;

            // Probar derecha
            String right = "" + row1 + (maxCol + 1);
            if (isValidCoordinate(row1, maxCol + 1) && !alreadyShot.contains(right)) return right;

        } else if (col1 == col2) {
            // --- VERTICAL ---
            char minRow = 'Z', maxRow = 'A';
            for(String h : hits) {
                char r = h.charAt(0);
                if(r < minRow) minRow = r;
                if(r > maxRow) maxRow = r;
            }

            // Probar arriba
            String up = "" + (char)(minRow - 1) + col1;
            if (isValidCoordinate((char)(minRow - 1), col1) && !alreadyShot.contains(up)) return up;

            // Probar abajo
            String down = "" + (char)(maxRow + 1) + col1;
            if (isValidCoordinate((char)(maxRow + 1), col1) && !alreadyShot.contains(down)) return down;
        }

        return null; // No se pudo deducir o los extremos están bloqueados/disparados
    }

    private List<String> getNeighbors(String coord) {
        List<String> neighbors = new ArrayList<>();
        char row = coord.charAt(0);
        int col = Integer.parseInt(coord.substring(1));

        // Añadir vecinos válidos
        if (row > 'A') neighbors.add("" + (char)(row - 1) + col); // Arriba
        if (row < 'J') neighbors.add("" + (char)(row + 1) + col); // Abajo
        if (col > 1)   neighbors.add("" + row + (col - 1));       // Izquierda
        if (col < 10)  neighbors.add("" + row + (col + 1));       // Derecha

        Collections.shuffle(neighbors); // Aleatoriedad para no ser predecible
        return neighbors;
    }

    private String generateRandomCoordinate(List<String> shots) {
        String coord;
        int attempts = 0;
        do {
            char row = (char) ('A' + random.nextInt(10)); // A-J
            int col = 1 + random.nextInt(10);             // 1-10
            coord = "" + row + col;
            attempts++;

            // Seguridad: Si intentamos 500 veces y no encontramos sitio (tablero casi lleno),
            // disparamos a la primera celda libre que encontremos.
            if (attempts > 500) {
                return findFirstFreeCell(shots);
            }

        } while (shots.contains(coord) || !isSmartRandomMove(coord, shots));

        return coord;
    }

    /**
     * Un movimiento "inteligente" evita disparar a celdas aisladas donde no cabe el barco más pequeño (2).
     * Esto es el "Parity Check" simplificado.
     */
    private boolean isSmartRandomMove(String coord, List<String> shots) {
        char row = coord.charAt(0);
        int col = Integer.parseInt(coord.substring(1));

        // Comprobamos si tiene AL MENOS un vecino libre.
        // Si todos los vecinos están disparados, es una celda aislada de 1x1. No dispares ahí.
        boolean hasFreeNeighbor =
                (isValidCoordinate(row, col-1) && !shots.contains("" + row + (col-1))) ||
                        (isValidCoordinate(row, col+1) && !shots.contains("" + row + (col+1))) ||
                        (isValidCoordinate((char)(row-1), col) && !shots.contains("" + (char)(row-1) + col)) ||
                        (isValidCoordinate((char)(row+1), col) && !shots.contains("" + (char)(row+1) + col));

        return hasFreeNeighbor;
    }

    private boolean isValidCoordinate(char row, int col) {
        return row >= 'A' && row <= 'J' && col >= 1 && col <= 10;
    }

    // Fallback de seguridad por si el tablero está casi lleno
    private String findFirstFreeCell(List<String> shots) {
        for (char r = 'A'; r <= 'J'; r++) {
            for (int c = 1; c <= 10; c++) {
                String coord = "" + r + c;
                if (!shots.contains(coord)) return coord;
            }
        }
        return null; // Tablero lleno (Game Over debería haber saltado antes)
    }
}