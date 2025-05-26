package gob.oax.cad.consumer.stream;

import gob.oax.cad.consumer.routing.AgentSessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAgentConnectionListener {

    private final AgentSessionRegistry agentSessionRegistry;

    @EventListener
    public void handleConnectEvent(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String agentId = accessor.getFirstNativeHeader("agent-id");
        String sessionId = accessor.getSessionId();

        if (agentId != null && sessionId != null) {
            // Guardamos el agent-id en los atributos de sesión para uso posterior
            accessor.getSessionAttributes().put("agent-id", agentId);

            agentSessionRegistry.register(agentId, sessionId);

            log.info("Agente conectado: {} (sesión={})", agentId, sessionId);
        } else {
            log.warn("Conexión WebSocket sin 'agent-id' o 'sessionId'");
        }
    }

    @EventListener
    public void handleDisconnectEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        if (sessionId != null) {
            agentSessionRegistry.unregister(sessionId);

            // Intentamos recuperar el agent-id desde los atributos de sesión
            Object agentIdObj = accessor.getSessionAttributes().get("agent-id");
            String agentId = agentIdObj != null ? agentIdObj.toString() : "desconocido";

            log.info("Sesión desconectada: {} (agente={})", sessionId, agentId);
        } else {
            log.warn("Desconexión sin sessionId");
        }
    }
}