package gob.oax.cad.consumer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Permitir Angular
        ;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Donde los clientes se suscriben
        config.enableSimpleBroker("/topic");

        // Prefijo requerido para mensajes enviados desde el frontend
        config.setApplicationDestinationPrefixes("/app");
    }
}
