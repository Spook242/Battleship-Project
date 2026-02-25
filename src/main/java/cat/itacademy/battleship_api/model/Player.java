package cat.itacademy.battleship_api.model;

import jakarta.persistence.*;
import lombok.*;


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


    @Builder.Default
    @Column(nullable = false)
    private int gamesPlayed = 0;

    @Builder.Default
    @Column(nullable = false)
    private int gamesWon = 0;

    @Column(nullable = false)
    private String password;

    public Player(String username) {

    }


}