package cat.itacademy.battleship_api.controller;

import cat.itacademy.battleship_api.dto.GameMoveMessage;
import cat.itacademy.battleship_api.model.enums.ShotResult;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {


    @MessageMapping("/play")

    @SendTo("/topic/game-progress")
    public GameMoveMessage playTurn(GameMoveMessage message) {


        System.out.println("Jugada recibida de: " + message.getPlayer() + " en " + message.getCoordinate());


        message.setResult(ShotResult.valueOf("PROCESSED_BY_SERVER"));
        return message;
    }
}