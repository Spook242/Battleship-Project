package cat.itacademy.battleship_api.dto;

import cat.itacademy.battleship_api.model.Ship;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class PlaceShipsRequest {
    private List<Ship> ships;
}