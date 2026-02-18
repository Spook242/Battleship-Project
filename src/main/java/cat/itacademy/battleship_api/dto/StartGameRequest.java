package cat.itacademy.battleship_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder // 👈 Muy útil para tests
@NoArgsConstructor
@AllArgsConstructor
public class StartGameRequest {

    @NotBlank(message = "Username is required") // 🛡️ Evita nombres vacíos o solo espacios
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    // ❌ HEMOS BORRADO EL GETTER MANUAL
    // public String getUsername() { ... }
    // ¿Por qué? Porque la anotación @Data de arriba YA lo genera automáticamente.
    // Escribirlo a mano es trabajar el doble. 😉
}