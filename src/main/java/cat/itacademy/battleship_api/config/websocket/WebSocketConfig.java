package cat.itacademy.battleship_api.config.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // 👈 Habilita el servidor de mensajería
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Este es el punto de entrada (la URL donde se conecta el frontend)
        // setAllowedOriginPatterns("*") es vital para que no te bloquee CORS
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // Permite conectarse aunque el navegador no soporte WS nativos
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefijo para los mensajes que van DESDE el servidor HACIA el cliente
        registry.enableSimpleBroker("/topic");

        // Prefijo para los mensajes que van DESDE el cliente HACIA el servidor
        registry.setApplicationDestinationPrefixes("/app");
    }
}