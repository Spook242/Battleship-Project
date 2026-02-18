package cat.itacademy.battleship_api.dto;

import cat.itacademy.battleship_api.model.Game;
import lombok.AllArgsConstructor;
import lombok.Builder; // 👈 Para crear el objeto fácilmente
import lombok.Data;
import lombok.NoArgsConstructor; // 👈 ¡Imprescindible para JSON!

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor // ⚠️ Vital: Jackson (la librería que convierte a JSON) a veces falla si no existe este constructor vacío.
public class GameStartResponse {

    private Game game;
    private String token;
}