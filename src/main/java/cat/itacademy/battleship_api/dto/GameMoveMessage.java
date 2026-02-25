package cat.itacademy.battleship_api.dto;

import cat.itacademy.battleship_api.model.enums.PlayerTurn;
import cat.itacademy.battleship_api.model.enums.ShotResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameMoveMessage {


    private PlayerTurn player;

    private String coordinate;

    private ShotResult result;
}