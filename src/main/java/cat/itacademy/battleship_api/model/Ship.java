package cat.itacademy.battleship_api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ship {


    private String type;

    private int size;


    @Builder.Default
    private List<String> cells = new ArrayList<>();

    @Builder.Default
    private List<String> hits = new ArrayList<>();

    private boolean sunk;

    public boolean receiveHit(String coordinate) {

        if (this.cells.contains(coordinate) && !this.hits.contains(coordinate)) {
            this.hits.add(coordinate);
            this.updateSunkStatus();
            return true;
        }
        return false;
    }


    private void updateSunkStatus() {

        if (this.hits.size() == this.size) {
            this.sunk = true;
        }
    }
}