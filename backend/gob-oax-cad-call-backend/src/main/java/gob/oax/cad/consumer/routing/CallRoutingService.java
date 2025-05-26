package gob.oax.cad.consumer.routing;

import gob.oax.cad.consumer.model.CallStreamEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallRoutingService {

    private final AgentSessionRegistry agentSessionRegistry;
    private final CallAssignmentRegistry callAssignmentRegistry;

    public boolean assignAgent(CallStreamEvent event) {
        return agentSessionRegistry.findAnyAvailableAgent()
                .map(agentId -> {
                    event.setAgentId(agentId);
                    callAssignmentRegistry.assign(event.getCallId(), agentId);
                    agentSessionRegistry.markBusy(agentId);

                    log.info("Llamada asignada a agente: {}", agentId);

                    return true;
                })
                .orElseGet(() -> {
                    log.warn("No hay agentes disponibles para: {}", event);
                    return false;
                });
    }
}