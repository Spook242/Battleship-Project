package cat.itacademy.battleship_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidGameActionException extends RuntimeException {

    public InvalidGameActionException(String message) {
        super(message);
    }
}