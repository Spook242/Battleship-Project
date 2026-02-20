package cat.itacademy.battleship_api.model;

import jakarta.persistence.*;
import lombok.*;

// 1. MEJORA CRÍTICA: Cambiamos @Data por @Getter y @Setter
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    // 2. MEJORA: Valores por defecto directos en la declaración y en el Builder
    @Builder.Default
    @Column(nullable = false)
    private int gamesPlayed = 0;

    @Builder.Default
    @Column(nullable = false)
    private int gamesWon = 0;

    public Player(String username) {
    }

    // ❌ HEMOS BORRADO EL CONSTRUCTOR MANUAL
    // Al poner " = 0" arriba y usar @Builder.Default, ya no necesitas escribir
    // un constructor a mano. El código es más limpio.
}