package gob.oax.cad.adapter.listener;

import gob.oax.cad.adapter.model.CallMetadata;
import gob.oax.cad.adapter.model.CallState;
import gob.oax.cad.adapter.model.CallStreamEvent;
import gob.oax.cad.adapter.model.EventSource;
import gob.oax.cad.adapter.util.CallUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.telephony.Call;
import javax.telephony.CallEvent;
import javax.telephony.CallListener;
import javax.telephony.Connection;
import javax.telephony.MetaEvent;
import java.time.Instant;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Listener para eventos de llamadas en sistemas Avaya usando JTAPI.
 * <p>
 * Procesa eventos de llamadas y los transforma en objetos {@link CallStreamEvent},
 * enviándolos a un consumidor para su posterior manejo.
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class CallLifecycleListener implements CallListener {

    /**
     * Consumidor que recibe los eventos de llamada procesados.
     */
    private final Consumer<CallStreamEvent> callEventConsumer;
    private final Map<String, CallMetadata> callRegistry;

    /**
     * Maneja el evento cuando una llamada está activa.
     * <p>
     * Extrae información relevante de la llamada y la envía al consumidor.
     * </p>
     *
     * @param event el evento de llamada activa recibido
     */
    @Override
    public void callActive(CallEvent event) {
        try {
            Call call = event.getCall();
            String callId = call.toString();

            // Check if the call is already registered to avoid duplicates
            callRegistry.putIfAbsent(callId, new CallMetadata(call, Instant.now()));

            Connection[] connections = call.getConnections();

            if (connections == null || connections.length == 0) return;

            for (Connection connection : connections) {
                String from = CallUtils.extractFrom(call);
                String to = CallUtils.extractTo(call);

                CallState state = mapState(connection.getState());

                boolean pending = (state == CallState.RINGING) && to.isBlank();

                CallStreamEvent callEvent = new CallStreamEvent(
                        callId,
                        from,
                        to,
                        state,
                        pending,
                        Instant.now(),
                        EventSource.ADAPTER,
                        null
                );

                callEventConsumer.accept(callEvent);

                log.info("Call [{}]: from [{}] to [{}] — State: {}", callId, from, to, state);
            }
        } catch (Exception e) {
            log.error("Error en callActive: {}", e.getMessage(), e);
        }
    }

    /**
     * Maneja el evento cuando una llamada es inválida o ha terminado.
     *
     * @param event el evento de llamada inválida recibido
     */
    @Override
    public void callInvalid(CallEvent event) {
        try {
            Call call = event.getCall();
            String callId = call.toString();

            CallStreamEvent callEvent = new CallStreamEvent(
                    callId,
                    "", "", CallState.DISCONNECTED,
                    false,
                    Instant.now(),
                    EventSource.ADAPTER,
                    "Llamada inválida o terminada"
            );

            callEventConsumer.accept(callEvent);
            callRegistry.remove(callId);

            log.info("Llamada terminada o inválida: {}", callEvent);
        } catch (Exception e) {
            log.warn("Error en callInvalid: {}", e.getMessage(), e);
        }

    }

    /**
     * Maneja el evento cuando la transmisión de eventos de llamada ha finalizado.
     *
     * @param event el evento de transmisión finalizada
     */
    @Override
    public void callEventTransmissionEnded(CallEvent event) {
        log.info("Transmisión finalizada para llamada: {}", event.getCall());
    }

    @Override
    public void singleCallMetaProgressStarted(MetaEvent metaEvent) {
    }

    @Override
    public void singleCallMetaProgressEnded(MetaEvent metaEvent) {

    }

    @Override
    public void singleCallMetaSnapshotStarted(MetaEvent metaEvent) {

    }

    @Override
    public void singleCallMetaSnapshotEnded(MetaEvent metaEvent) {

    }

    @Override
    public void multiCallMetaMergeStarted(MetaEvent metaEvent) {

    }

    @Override
    public void multiCallMetaMergeEnded(MetaEvent metaEvent) {

    }

    @Override
    public void multiCallMetaTransferStarted(MetaEvent metaEvent) {

    }

    @Override
    public void multiCallMetaTransferEnded(MetaEvent metaEvent) {

    }

    private CallState mapState(int state) {
        return switch (state) {
            case Connection.ALERTING -> CallState.RINGING;
            case Connection.CONNECTED -> CallState.CONNECTED;
            case Connection.DISCONNECTED -> CallState.DISCONNECTED;
            case Connection.FAILED -> CallState.FAILED;
            default -> CallState.UNKNOWN;
        };
    }
}