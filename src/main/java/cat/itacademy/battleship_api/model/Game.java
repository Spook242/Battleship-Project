package cat.itacademy.battleship_api.model;

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

    private String status;

    private String turn;

    private String winner;

    private Board playerBoard;

    private Board cpuBoard;

    private List<String> cpuPendingTargets = new ArrayList<>();

}