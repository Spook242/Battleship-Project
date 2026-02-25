package cat.itacademy.battleship_api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Board {


    @Builder.Default
    private List<Ship> ships = new ArrayList<>();

    @Builder.Default
    private List<String> shotsReceived = new ArrayList<>();


    public void addShip(Ship ship) {
        this.ships.add(ship);
    }


    public void receiveShot(String coordinate) {
        if (!this.shotsReceived.contains(coordinate)) {
            this.shotsReceived.add(coordinate);
        }
    }


    public boolean areAllShipsSunk() {
        if (this.ships.isEmpty()) return false;
        return this.ships.stream().allMatch(Ship::isSunk);
    }
}