package cat.itacademy.battleship_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameMoveMessage {
    private String player;
    private String coordinate; // Ej: "A1"
    private String result;     // Ej: "HIT", "MISS"
}