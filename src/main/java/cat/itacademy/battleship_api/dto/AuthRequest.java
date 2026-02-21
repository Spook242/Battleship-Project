package cat.itacademy.battleship_api.dto;

import lombok.Data;

@Data // Aquí @Data está bien porque es un objeto simple de transferencia
public class AuthRequest {
    private String username;
    private String password;
}