package gob.oax.cad.adapter.listener.impl;

import gob.oax.cad.adapter.listener.CallMonitoringService;
import gob.oax.cad.adapter.model.CallMetadata;
import gob.oax.cad.adapter.model.CallState;
import gob.oax.cad.adapter.model.CallStreamEvent;
import gob.oax.cad.adapter.model.EventSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.telephony.Provider;
import java.time.Instant;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class SimulatedCallMonitoringService implements CallMonitoringService {

    private final Consumer<CallStreamEvent> callEventConsumer;

    @Override
    @EventListener(ContextRefreshedEvent.class)
    public boolean initialize() {
        log.info("SimulatedCallMonitoringService initialized (dev)");

        return true;
    }

    @Override
    public void routeCall(String callId, String terminal) {
        log.info("Simulando enrutamiento de llamada {} a {}", callId, terminal);

        emitSimulatedEvent(callId, terminal, CallState.CONNECTED, "Conexión simulada");
    }

    @Override
    public void terminateCall(String callId) {
        log.info("Simulando colgado de llamada {}", callId);

        emitSimulatedEvent(callId, null, CallState.DISCONNECTED, "Desconexión simulada");
    }

    @Override
    public void holdCall(String callId) {
        log.info("Simulando hold en llamada {}", callId);
    }

    @Override
    public void unholdCall(String callId) {
        log.info("Simulando unhold en llamada {}", callId);
    }

    @Override
    public void transferCall(String sourceCallId, String targetCallId) {
        log.info("Simulando transferencia de llamada {} a {}", sourceCallId, targetCallId);
    }

    @Override
    public void addToConference(String callId, String newParticipantExt) {
        log.info("Simulando agregar a conferencia: llamada {}, nuevo participante {}", callId, newParticipantExt);
    }

    @Override
    public Map<String, CallMetadata> getActiveCalls() {
        return Map.of();
    }

    @Override
    public Provider getProvider() {
        return null;
    }

    private void emitSimulatedEvent(String callId, String agentId, CallState state, String notes) {
        CallStreamEvent event = new CallStreamEvent();
        event.setCallId(callId);
        event.setAgentId(agentId);
        event.setState(state);
        event.setTimestamp(Instant.now());
        event.setSource(EventSource.MANUAL);
        event.setNotes(notes);

        callEventConsumer.accept(event);
    }
}
