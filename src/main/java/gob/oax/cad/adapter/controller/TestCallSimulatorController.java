package gob.oax.cad.adapter.controller;

import gob.oax.cad.adapter.model.CallState;
import gob.oax.cad.adapter.model.CallStreamEvent;
import gob.oax.cad.adapter.model.EventSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.function.Consumer;

@Slf4j
@RestController
@RequestMapping("/api/calls")
@RequiredArgsConstructor
public class TestCallSimulatorController {

    private final Consumer<CallStreamEvent> callEventConsumer;

    @PostMapping("/ringing/{callId}")
    public ResponseEntity<Void> simulateRinging(@PathVariable String callId) {
        log.info("Simulando llamada entrante con callId={}", callId);

        return emit(callId, CallState.RINGING, "Llamada entrante simulada");
    }

    @PostMapping("/connected/{callId}")
    public ResponseEntity<Void> simulateConnected(@PathVariable String callId) {
        log.info("Simulando llamada conectada con callId={}", callId);

        return emit(callId, CallState.CONNECTED, "Llamada conectada simulada");
    }

    @PostMapping("/disconnected/{callId}")
    public ResponseEntity<Void> simulateDisconnected(@PathVariable String callId) {
        log.info("Simulando llamada finalizada con callId={}", callId);

        return emit(callId, CallState.DISCONNECTED, "Llamada finalizada simulada");
    }

    private ResponseEntity<Void> emit(String callId, CallState state, String note) {
        CallStreamEvent event = new CallStreamEvent(
                callId,
                "+529511122334",
                state == CallState.RINGING ? null : "8801",
                state,
                false,
                Instant.now(),
                EventSource.ADAPTER,
                note
        );

        log.info("Emitiendo evento: {}", event);

        callEventConsumer.accept(event);

        return ResponseEntity.ok().build();
    }

}
