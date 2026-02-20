package cat.itacademy.battleship_api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Board {

    // 1. Limpieza: Quitamos la ruta larga (cat.itacademy...)
    @Builder.Default
    private List<Ship> ships = new ArrayList<>();

    @Builder.Default
    private List<String> shotsReceived = new ArrayList<>();

    // 2. Método auxiliar limpio
    public void addShip(Ship ship) {
        this.ships.add(ship);
    }

    // --- 3. LÓGICA DE DOMINIO (¡NUEVO Y RECOMENDADO!) ---

    // El tablero debería encargarse de registrar los disparos
    public void receiveShot(String coordinate) {
        if (!this.shotsReceived.contains(coordinate)) {
            this.shotsReceived.add(coordinate);
        }
    }

    // El tablero debería saber si ha perdido la partida
    public boolean areAllShipsSunk() {
        if (this.ships.isEmpty()) return false; // Si no hay barcos, no ha perdido (está en setup)
        return this.ships.stream().allMatch(Ship::isSunk);
    }
}