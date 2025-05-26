package gob.oax.cad.consumer.stream;

import gob.oax.cad.consumer.model.CallStreamEvent;
import gob.oax.cad.consumer.routing.CallAssignmentRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallEventNotifier {

    private final SimpMessagingTemplate messagingTemplate;
    private final CallAssignmentRegistry callAssignmentRegistry;

    public void sendToAgent(CallStreamEvent event) {
        String callId = event.getCallId();
        String agentId = callAssignmentRegistry.getAssignedAgent(callId).orElse(null);

        if (agentId == null) {
            log.warn("No se puede enviar el evento, agentId no definido: {}", event);
            return;
        }

        String destination = "/topic/agent/" + agentId;

        log.info("Enviando evento a {}: {}", destination, event);

        messagingTemplate.convertAndSend(destination, event);
    }
}
