package cat.itacademy.battleship_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder; // 👈 Útil para tests
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameStartRequest {

    @NotBlank(message = "Username is required") // 🚫 Prohíbe nulos, vacíos "" y espacios en blanco "   "
    @Size(min = 3, max = 15, message = "Username must be between 3 and 15 characters") // 📏 Controla la longitud
    private String username;
}