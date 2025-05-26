package gob.oax.cad.consumer.stream;

import gob.oax.cad.consumer.model.CallStreamEvent;
import gob.oax.cad.consumer.routing.AgentSessionRegistry;
import gob.oax.cad.consumer.routing.CallAssignmentRegistry;
import gob.oax.cad.consumer.routing.CallRoutingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallEventHandlerService {

    private final CallEventNotifier callEventNotifier;
    private final CallRoutingService callRoutingService;
    private final AgentSessionRegistry agentSessionRegistry;
    private final CallAssignmentRegistry callAssignmentRegistry;

    public void handleEvent(CallStreamEvent event) {
        log.info("Evento recibido desde el adapter: {}", event);

        switch (event.getState()) {
            case RINGING -> handleRinging(event);
            case CONNECTED -> handleConnected(event);
            case DISCONNECTED -> handleDisconnected(event);
            default -> log.warn("Estado de llamada no manejado: {}", event.getState());
        }

        if (event.getAgentId() != null && !event.getAgentId().isBlank()) {
            callEventNotifier.sendToAgent(event);
        } else {
            log.warn("Evento no enviado: no se pudo asignar un agente al evento: {}", event);
        }
    }

    private void handleRinging(CallStreamEvent event) {
        log.info("Llamada entrante desde {} (callId={})", event.getFrom(), event.getCallId());

        boolean assigned = callRoutingService.assignAgent(event);
        if (!assigned) {
            log.warn("No se pudo asignar un agente disponible a la llamada {}", event.getCallId());
        }
    }

    private void handleConnected(CallStreamEvent event) {
        log.info("Llamada conectada con {} (callId={})", event.getTo(), event.getCallId());

        callEventNotifier.sendToAgent(event);
    }

    private void handleDisconnected(CallStreamEvent event) {
        String callId = event.getCallId();

        log.info("Llamada finalizada (callId={})", callId);

        callAssignmentRegistry.getAssignedAgent(callId).ifPresent(agentId -> {
            agentSessionRegistry.markAvailable(agentId);
            log.info("✅ Agente {} marcado como disponible", agentId);
        });

        callEventNotifier.sendToAgent(event);

        callAssignmentRegistry.unassign(callId);
    }
}