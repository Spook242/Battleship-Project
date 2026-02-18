package cat.itacademy.battleship_api.dto;

import cat.itacademy.battleship_api.model.enums.PlayerTurn; // O el enum que uses
import cat.itacademy.battleship_api.model.enums.ShotResult; // Nuevo enum sugerido
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder // 👈 ¡Novedad útil!
@AllArgsConstructor
@NoArgsConstructor
public class GameMoveMessage {

    // Usamos Enums en lugar de String para evitar errores de texto ("CPU" vs "cpu")
    private PlayerTurn player;   // Valores: PLAYER, CPU

    private String coordinate;   // "A1", "B5"... (String está bien aquí)

    private ShotResult result;   // Valores: HIT, MISS, SUNK
}