package cat.itacademy.battleship_api.model;

import cat.itacademy.battleship_api.model.enums.GameStatus;
import cat.itacademy.battleship_api.model.enums.PlayerTurn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "games")
public class Game {

    @Id
    private String id;

    private Long playerId;


    @Builder.Default
    private GameStatus status = GameStatus.SETUP;

    @Builder.Default
    private PlayerTurn turn = PlayerTurn.PLAYER;

    private String winner;


    @Builder.Default
    private Board playerBoard = new Board();

    @Builder.Default
    private Board cpuBoard = new Board();


    @Builder.Default
    private List<String> cpuPendingTargets = new ArrayList<>();
}