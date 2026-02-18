package cat.itacademy.battleship_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor // ⚠️ Vital para que Jackson (JSON) pueda leerlo si hace falta
@AllArgsConstructor // ⚠️ Vital si usas consultas JPQL tipo "SELECT new cat.itacademy...DTO(u.name, count(g))"
public class PlayerScoreDTO {

    private String username;
    private long wins;
}