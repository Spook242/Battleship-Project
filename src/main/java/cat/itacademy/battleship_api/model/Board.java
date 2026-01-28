package cat.itacademy.battleship_api.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Board {
    private List<cat.itacademy.battleship_api.model.Ship> ships = new ArrayList<>();
    private List<String> shotsReceived = new ArrayList<>();

    public void addShip(cat.itacademy.battleship_api.model.Ship ship) {
        this.ships.add(ship);
    }
}