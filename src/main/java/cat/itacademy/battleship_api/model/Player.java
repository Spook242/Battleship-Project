package cat.itacademy.battleship_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
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

    private int gamesPlayed;
    private int gamesWon;

    public Player(String username) {
        this.username = username;
        this.gamesPlayed = 0;
        this.gamesWon = 0;
    }
}