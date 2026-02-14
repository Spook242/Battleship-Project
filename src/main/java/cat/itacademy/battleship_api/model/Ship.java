package cat.itacademy.battleship_api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ship {
    private String type;
    private int size;
    private List<String> cells;
    private List<String> hits;
    private boolean sunk;
}