package cat.itacademy.battleship_api.repository;

import cat.itacademy.battleship_api.model.Game;
import cat.itacademy.battleship_api.model.enums.GameStatus; // 👈 Importamos nuestro Enum
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends MongoRepository<Game, String> {

    // 1. Mantenemos 'winner' como String porque en el GameService usamos "PLAYER" o "CPU"
    long countByPlayerIdAndWinner(Long playerId, String winner);

    // 2. MEJORA: Exigimos el Enum GameStatus en lugar de un String libre
    List<Game> findByPlayerIdAndStatus(Long playerId, GameStatus status);
}