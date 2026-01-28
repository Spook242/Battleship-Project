package cat.itacademy.battleship_api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ship {
    private String type;         // Ej: "Portaviones", "Submarino"
    private int size;            // Tamaño (5, 4, 3...)
    private List<String> cells;  // Coordenadas ocupadas: ["A1", "A2", "A3"]
    private List<String> hits;   // Dónde nos han dado: ["A2"]
    private boolean sunk;        // ¿Hundido?
}