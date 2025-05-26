package gob.oax.cad.consumer.stream;

import gob.oax.cad.consumer.config.CallAdapterClient;
import gob.oax.cad.consumer.model.AgentActionMessage;
import gob.oax.cad.consumer.model.RouteCallRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AgentActionWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final CallAdapterClient callAdapterClient;

    @MessageMapping("/agent/{agentId}/action")
    public void handleAgentAction(@DestinationVariable String agentId, AgentActionMessage message) {
        log.info("🎮 Acción recibida del agente {}: {}", agentId, message);

        try {
            String callId = message.getCallId();
            String action = message.getAction().toUpperCase();

            switch (action) {
                case "ANSWER" -> {
                    RouteCallRequest request = new RouteCallRequest();
                    request.setCallId(callId);
                    request.setTerminal(agentId);
                    request.setAgentId(agentId);

                    callAdapterClient.routeCall(request);

                    log.info("Enrutamiento solicitado para llamada {} al agente {}", callId, agentId);
                }

                case "HANGUP" -> {
                    callAdapterClient.endCall(callId);
                    log.info("Terminación solicitada para llamada {}", callId);
                }

                case "HOLD" -> {
                    callAdapterClient.holdCall(callId);
                    log.info("Solicitud de hold para llamada {}", callId);
                }

                case "UNHOLD" -> {
                    callAdapterClient.unholdCall(callId);
                    log.info("Solicitud de unhold para llamada {}", callId);
                }

                default -> log.warn("Acción no reconocida o no implementada: {}", action);
            }

        } catch (Exception e) {
            log.error("Error procesando la acción del agente {}: {}", agentId, e.getMessage(), e);
        }

        // Feedback opcional al agente
        messagingTemplate.convertAndSend("/topic/agent/" + agentId, message);
    }
}