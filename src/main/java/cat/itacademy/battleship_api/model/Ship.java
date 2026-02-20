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

    // PRO-TIP: En el futuro, 'type' podría ser un Enum (ej: Carrier, Submarine)
    private String type;

    private int size;

    // 1. SEGURIDAD: Prevenimos NullPointerExceptions al crear barcos
    @Builder.Default
    private List<String> cells = new ArrayList<>();

    @Builder.Default
    private List<String> hits = new ArrayList<>();

    private boolean sunk;

    // --- 2. LÓGICA DE DOMINIO (Inteligencia del Barco) ---

    /**
     * Comprueba si el disparo ha dado en este barco.
     * Si es así, registra el impacto y comprueba si se ha hundido.
     */
    public boolean receiveHit(String coordinate) {
        // Si la coordenada pertenece al barco y no le habíamos dado ya ahí...
        if (this.cells.contains(coordinate) && !this.hits.contains(coordinate)) {
            this.hits.add(coordinate);
            this.updateSunkStatus(); // Comprobamos si se ha hundido
            return true; // ¡Tocado!
        }
        return false; // Agua (o ya le habíamos dado)
    }

    /**
     * Método privado que actualiza el estado 'sunk' automáticamente.
     */
    private void updateSunkStatus() {
        // El barco se hunde si la cantidad de impactos es igual a su tamaño
        if (this.hits.size() == this.size) {
            this.sunk = true;
        }
    }
}