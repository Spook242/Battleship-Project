package cat.itacademy.battleship_api.service;

import cat.itacademy.battleship_api.model.Board;
import cat.itacademy.battleship_api.model.Ship;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class BoardService {

    public boolean processShot(Board board, String coordinate) {
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

    public void placeShipsRandomly(Board board) {
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