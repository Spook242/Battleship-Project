package cat.itacademy.battleship_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerScoreDTO {
    private String username;
    private long wins;
}