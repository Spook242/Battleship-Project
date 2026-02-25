package cat.itacademy.battleship_api.service;

import cat.itacademy.battleship_api.model.Board;
import cat.itacademy.battleship_api.model.Ship;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BattleshipAiService {

    private static final int MAX_HUNT_ATTEMPTS = 5000;
    private final Random random = new Random();

    public String calculateCpuTarget(Board opponentBoard) {
        List<String> shotsFired = opponentBoard.getShotsReceived();


        List<String> openHits = getOpenHits(opponentBoard);

        if (!openHits.isEmpty()) {
            if (openHits.size() >= 2) {
                String lineTarget = getLineStrategyTarget(openHits, shotsFired);
                if (lineTarget != null) return lineTarget;
            }

            for (String hit : openHits) {
                for (String neighbor : getNeighbors(hit)) {
                    if (!shotsFired.contains(neighbor)) return neighbor;
                }
            }
        }


        int minShipSize = getSmallestAliveShipSize(opponentBoard);
        return generateSmartGapCoordinate(shotsFired, minShipSize);
    }


    private List<String> getOpenHits(Board opponentBoard) {
        return opponentBoard.getShips().stream().filter(ship -> !ship.isSunk()).flatMap(ship -> ship.getHits().stream()).collect(Collectors.toList());
    }


    private int getSmallestAliveShipSize(Board board) {
        return board.getShips().stream().filter(ship -> !ship.isSunk()).mapToInt(Ship::getSize).min().orElse(2);
    }

    private String generateSmartGapCoordinate(List<String> shots, int targetSize) {
        int attempts = 0;

        while (attempts < MAX_HUNT_ATTEMPTS) {
            char row = (char) ('A' + random.nextInt(10));
            int col = 1 + random.nextInt(10);
            String coord = "" + row + col;
            attempts++;

            if (!shots.contains(coord) && fitsInGap(coord, shots, targetSize)) {
                return coord;
            }
        }


        return findFirstFreeCell(shots);
    }

    private boolean fitsInGap(String coord, List<String> shots, int targetSize) {
        char row = coord.charAt(0);
        int col = Integer.parseInt(coord.substring(1));


        int freeLeft = countFreeSpaces(row, col, 0, -1, shots);
        int freeRight = countFreeSpaces(row, col, 0, 1, shots);
        if ((freeLeft + 1 + freeRight) >= targetSize) return true;


        int freeUp = countFreeSpaces(row, col, -1, 0, shots);
        int freeDown = countFreeSpaces(row, col, 1, 0, shots);
        return (freeUp + 1 + freeDown) >= targetSize;
    }

    private int countFreeSpaces(char startRow, int startCol, int dRow, int dCol, List<String> shots) {
        int count = 0;
        char currentRow = (char) (startRow + dRow);
        int currentCol = startCol + dCol;

        while (isValidCoordinate(currentRow, currentCol)) {
            String coord = "" + currentRow + currentCol;
            if (shots.contains(coord)) break;
            count++;
            currentRow = (char) (currentRow + dRow);
            currentCol += dCol;
        }
        return count;
    }


    private String getLineStrategyTarget(List<String> hits, List<String> alreadyShot) {
        Collections.sort(hits);
        String first = hits.get(0);
        String second = hits.get(1);

        char row1 = first.charAt(0);
        char row2 = second.charAt(0);
        int col1 = Integer.parseInt(first.substring(1));
        int col2 = Integer.parseInt(second.substring(1));

        if (row1 == row2) {
            int minCol = hits.stream().mapToInt(h -> Integer.parseInt(h.substring(1))).min().orElse(11);
            int maxCol = hits.stream().mapToInt(h -> Integer.parseInt(h.substring(1))).max().orElse(0);

            String left = "" + row1 + (minCol - 1);
            if (isValidCoordinate(row1, minCol - 1) && !alreadyShot.contains(left)) return left;

            String right = "" + row1 + (maxCol + 1);
            if (isValidCoordinate(row1, maxCol + 1) && !alreadyShot.contains(right)) return right;

        } else if (col1 == col2) {
            char minRow = (char) hits.stream().mapToInt(h -> h.charAt(0)).min().orElse('Z');
            char maxRow = (char) hits.stream().mapToInt(h -> h.charAt(0)).max().orElse('A');

            String up = "" + (char) (minRow - 1) + col1;
            if (isValidCoordinate((char) (minRow - 1), col1) && !alreadyShot.contains(up)) return up;

            String down = "" + (char) (maxRow + 1) + col1;
            if (isValidCoordinate((char) (maxRow + 1), col1) && !alreadyShot.contains(down)) return down;
        }
        return null;
    }

    private List<String> getNeighbors(String coord) {
        List<String> neighbors = new ArrayList<>();
        char row = coord.charAt(0);
        int col = Integer.parseInt(coord.substring(1));

        if (row > 'A') neighbors.add("" + (char) (row - 1) + col);
        if (row < 'J') neighbors.add("" + (char) (row + 1) + col);
        if (col > 1) neighbors.add("" + row + (col - 1));
        if (col < 10) neighbors.add("" + row + (col + 1));

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
        return null;
    }
}