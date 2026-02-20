package cat.itacademy.battleship_api.model;

import cat.itacademy.battleship_api.model.enums.GameStatus; // Enum Nuevo
import cat.itacademy.battleship_api.model.enums.PlayerTurn; // Enum Nuevo
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
    private String id; // ID de MongoDB (String es correcto)

    private Long playerId; // Enlace a PostgreSQL (Correcto)

    // 1. MEJORA: Usamos Enums para evitar errores de texto
    @Builder.Default
    private GameStatus status = GameStatus.SETUP;

    @Builder.Default
    private PlayerTurn turn = PlayerTurn.PLAYER;

    private String winner; // Puede ser String (nombre del usuario) o Enum

    // 2. SEGURIDAD: Inicializamos los tableros para evitar NullPointerException
    @Builder.Default
    private Board playerBoard = new Board();

    @Builder.Default
    private Board cpuBoard = new Board();

    // 3. IA: Memoria de la CPU para modo "Caza" (Target mode)
    @Builder.Default
    private List<String> cpuPendingTargets = new ArrayList<>();
}