package cat.itacademy.battleship_api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "games")
public class Game {

    @Id
    private String id; // Mongo usa String para IDs por defecto

    private Long playerId; // Referencia al ID de MySQL del humano

    // Estados posibles: "PREPARING", "PLAYING", "FINISHED"
    private String status;

    // ¿De quién es el turno? "PLAYER" o "CPU"
    private String turn;

    private String winner; // null, "PLAYER" o "CPU"

    // Los dos tableros
    private Board playerBoard;
    private Board cpuBoard;
}