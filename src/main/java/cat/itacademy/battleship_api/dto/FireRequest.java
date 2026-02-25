package cat.itacademy.battleship_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FireRequest {


    @NotBlank(message = "Coordinate is required")
    @Pattern(regexp = "^[A-J](10|[1-9])$", message = "Invalid coordinate format. Use A1, B5, J10...")
    private String coordinate;
}