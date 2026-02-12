package cat.itacademy.battleship_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Al poner esto, si lanzas esta excepción, Spring devolverá automáticamente un error 400 (Bad Request)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidGameActionException extends RuntimeException {

    public InvalidGameActionException(String message) {
        super(message);
    }
}