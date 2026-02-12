package cat.itacademy.battleship_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 1. Capturar errores de juego (Bad Request 400)
    @ExceptionHandler(InvalidGameActionException.class)
    public ResponseEntity<Map<String, String>> handleInvalidGameAction(InvalidGameActionException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage()); // Solo enviamos el texto, no el objeto entero
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 2. Capturar cualquier otro error inesperado (Internal Server Error 500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Error interno del servidor: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}