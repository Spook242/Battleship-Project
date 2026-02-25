package cat.itacademy.battleship_api.dto;

import cat.itacademy.battleship_api.model.Game;
import lombok.AllArgsConstructor;
import lombok.Builder; 
import lombok.Data;
import lombok.NoArgsConstructor; 

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor 
public class GameStartResponse {

    private Game game;
    private String token;
}