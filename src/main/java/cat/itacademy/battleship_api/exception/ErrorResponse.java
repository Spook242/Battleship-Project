package cat.itacademy.battleship_api.exception;

public record ErrorResponse(
        int status,
        String message,
        long timestamp
) {}