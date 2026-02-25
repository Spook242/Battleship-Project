package cat.itacademy.battleship_api.repository;

import cat.itacademy.battleship_api.model.Game;
import cat.itacademy.battleship_api.model.enums.GameStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends MongoRepository<Game, String> {

    long countByPlayerIdAndWinner(Long playerId, String winner);

    List<Game> findByPlayerIdAndStatus(Long playerId, GameStatus status);
}