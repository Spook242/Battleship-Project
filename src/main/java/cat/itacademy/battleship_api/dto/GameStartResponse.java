package cat.itacademy.battleship_api.dto;

import cat.itacademy.battleship_api.model.Game;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GameStartResponse {
    private Game game;
    private String token;
}