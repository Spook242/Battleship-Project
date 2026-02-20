package cat.itacademy.battleship_api.repository;

import cat.itacademy.battleship_api.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // Opcional, pero muy buena práctica visual
public interface PlayerRepository extends JpaRepository<Player, Long> {

    // Al usar Optional, nos protegemos de los temidos NullPointerException
    // si el usuario (username) no existe en la base de datos PostgreSQL.
    Optional<Player> findByUsername(String username);
}