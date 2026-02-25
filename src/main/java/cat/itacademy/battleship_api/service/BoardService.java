package cat.itacademy.battleship_api.service;

import cat.itacademy.battleship_api.exception.InvalidMoveException;
import cat.itacademy.battleship_api.model.Board;
import cat.itacademy.battleship_api.model.Ship;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class BoardService {

    private final Random random = new Random();


    public boolean processShot(Board board, String coordinate) {


        if (board.getShotsReceived().contains(coordinate)) {


            throw new InvalidMoveException("Ya has disparado a la coordenada " + coordinate);
        }


        board.receiveShot(coordinate);


        for (Ship ship : board.getShips()) {
            if (ship.receiveHit(coordinate)) {
                return true;
            }
        }

        return false;
    }


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


        Ship newShip = Ship.builder().type("Ship-" + size).size(size).cells(shipCells).build();

        board.addShip(newShip);
        return true;
    }

    private boolean isOccupied(Board board, String coordinate) {

        return board.getShips().stream().anyMatch(ship -> ship.getCells().contains(coordinate));
    }

    private String toCoordinate(int row, int col) {
        char rowChar = (char) ('A' + row);
        return rowChar + String.valueOf(col + 1);
    }
}