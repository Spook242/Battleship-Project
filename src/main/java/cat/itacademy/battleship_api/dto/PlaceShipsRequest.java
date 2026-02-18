package cat.itacademy.battleship_api.dto;

import cat.itacademy.battleship_api.model.Ship;
import jakarta.validation.Valid; // 👈 Para validar el interior de la lista
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaceShipsRequest {

    @NotNull(message = "Ships list cannot be null") // 🚫 No permite nulos
    @Size(min = 5, max = 5, message = "You must place exactly 5 ships") // 📏 Regla del Battleship clásico
    @Valid // 🕵️‍♂️ ¡IMPORTANTE! Valida cada objeto 'Ship' dentro de la lista
    private List<Ship> ships;
}