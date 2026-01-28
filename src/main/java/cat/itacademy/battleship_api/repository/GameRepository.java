package cat.itacademy.battleship_api.repository;

import cat.itacademy.battleship_api.model.Game;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends MongoRepository<Game, String> {
    List<Game> findByPlayerIdAndStatus(Long playerId, String status);
}