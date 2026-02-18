package cat.itacademy.battleship_api.controller;

import cat.itacademy.battleship_api.dto.GameMoveMessage;
import cat.itacademy.battleship_api.model.enums.ShotResult;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    // Cuando el cliente envíe un mensaje a "/app/play"
    @MessageMapping("/play")
    // Lo reenviaremos automáticamente a todos los suscritos a "/topic/game-progress"
    @SendTo("/topic/game-progress")
    public GameMoveMessage playTurn(GameMoveMessage message) {
        // Aquí iría la lógica real (llamar al servicio, comprobar hit/miss)
        // De momento, solo hacemos de "eco" para probar la conexión
        System.out.println("Jugada recibida de: " + message.getPlayer() + " en " + message.getCoordinate());

        // Simulamos respuesta
        message.setResult(ShotResult.valueOf("PROCESSED_BY_SERVER"));
        return message;
    }
}