package gob.oax.cad.adapter.listener;

import com.avaya.jtapi.tsapi.LucentCall;
import com.avaya.jtapi.tsapi.LucentTerminalConnection;
import com.avaya.jtapi.tsapi.LucentV5Call;
import gob.oax.cad.adapter.config.JtapiProperties;
import gob.oax.cad.adapter.model.CallMetadata;
import gob.oax.cad.adapter.model.CallStreamEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.telephony.Address;
import javax.telephony.Call;
import javax.telephony.CallObserver;
import javax.telephony.Connection;
import javax.telephony.JtapiPeer;
import javax.telephony.JtapiPeerFactory;
import javax.telephony.Provider;
import javax.telephony.Terminal;
import javax.telephony.TerminalConnection;
import javax.telephony.events.CallEv;
import javax.telephony.events.ConnAlertingEv;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Componente encargado de inicializar y registrar un listener JTAPI para monitorear eventos de llamadas
 * en un terminal específico, utilizando las propiedades configuradas.
 * <p>
 * Al inicializarse, establece la conexión con el proveedor JTAPI, obtiene el terminal configurado
 * y registra un listener para eventos de llamadas, enviando los eventos a un consumidor proporcionado.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JtapiCallMonitoringService {

    private final JtapiProperties properties;
    private final Consumer<CallStreamEvent> callEventConsumer;

    private Provider provider;
    private final Map<String, CallMetadata> activeCalls = new ConcurrentHashMap<>();

    /**
     * Inicializa el listener JTAPI al arrancar el componente.
     * <p>
     * Establece la conexión con el proveedor JTAPI usando las credenciales configuradas,
     * obtiene el terminal y registra los listeners necesarios para monitorear eventos de llamadas.
     * </p>
     */
    @EventListener(ContextRefreshedEvent.class)
    public boolean startJtapiListener() {
        try {
            log.info("Iniciando JTAPI listener");

            // Create JTAPI provider connection string
            String credentials = String.format("%s;login=%s;passwd=%s",
                    properties.getProvider(),
                    properties.getLogin(),
                    properties.getPassword());

            log.info("Connectando a JTAPI provider: {}", credentials);

            // Connect to JTAPI provider
            JtapiPeer peer = JtapiPeerFactory.getJtapiPeer(null);
            // Nos conectamos al proveedor JTAPI
            provider = peer.getProvider(credentials);

            log.info("Conectado a JTAPI provider: {}", provider.getName());

            // Agregamos un listener para el proveedor
            provider.addProviderListener(new ProviderListenerAdapter());

            log.info("Listener agregado al proveedor JTAPI: {}", provider.getName());

            Address entryAddress = provider.getAddress(properties.getDevice());

            entryAddress.addCallObserver(new CallObserver() {
                @Override
                public void callChangedEvent(CallEv[] events) {
                    try {
                        for (CallEv ev : events) {
                            if (ev instanceof ConnAlertingEv) {
                                Call call = ev.getCall();
                                String callId = call.toString();

                                log.info("Incoming call [{}] detected on address [{}]", callId, entryAddress.getName());

                                // Registrar la llamada en el mapa de llamadas activas si no existe
                                activeCalls.putIfAbsent(callId, new CallMetadata(call, Instant.now()));

                                call.addCallListener(new CallLifecycleListener(callEventConsumer, activeCalls));
                            }
                        }
                    } catch (Exception ex) {
                        log.error("Error processing call event: {}", ex.getMessage(), ex);
                    }
                }
            });

            log.info("JTAPI adapter is now listening for calls on address [{}]", entryAddress.getName());

            return true;
        } catch (Exception ex) {
            provider = null;

            log.error("Error al inicializar JTAPI (Listener): {}", ex.getMessage(), ex);

            return false;
        }
    }

    // Método para enrutar una llamada a un número de extensión específico
    public void routeCall(String callId, String targetExtension) throws Exception {
        CallMetadata metadata = activeCalls.get(callId);

        if (metadata == null) {
            log.warn("No active call found with ID: {}", callId);
            throw new IllegalArgumentException("Call not found: " + callId);
        }

        Call call = metadata.getCall();

        String originDevice = properties.getDevice();
        Terminal originTerminal = provider.getTerminal(originDevice);
        Address originAddress = provider.getAddress(originDevice);

        if (originTerminal == null || originAddress == null) {
            throw new IllegalStateException("Origin terminal or address not found for device: " + originDevice);
        }

        try {
            log.info("Connecting call [{}] from [{}] to [{}]", callId, originDevice, targetExtension);

            call.connect(originTerminal, originAddress, targetExtension); // Avaya toma el tercer argumento como el número real de destino

            log.info("Call [{}] successfully routed to [{}]", callId, targetExtension);

            activeCalls.remove(callId);
        } catch (Exception ex) {
            log.error("Error routing call {} to {}: {}", callId, targetExtension, ex.getMessage(), ex);
            throw ex;
        }
    }

    public void transferCall(String callId, String toExtension) throws Exception {
        CallMetadata metadata = activeCalls.get(callId);
        if (metadata == null) throw new IllegalArgumentException("Call not found");

        Call call = metadata.getCall();

        if (call instanceof LucentV5Call lucentCall) {
            lucentCall.transfer(toExtension);

            log.info("Call {} transferred to {}", callId, toExtension);
        } else {
            throw new UnsupportedOperationException("Call does not support transfer");
        }
    }

    public void holdCall(String callId) throws Exception {
        CallMetadata metadata = activeCalls.get(callId);
        if (metadata == null) throw new IllegalArgumentException("Call not found");

        Call call = metadata.getCall();
        Connection[] connections = call.getConnections();

        for (Connection conn : connections) {
            TerminalConnection[] terminals = conn.getTerminalConnections();
            if (terminals != null) {
                for (TerminalConnection term : terminals) {
                    if (term instanceof LucentTerminalConnection lucentTerm) {
                        lucentTerm.hold();

                        log.info("Call placed on hold at terminal: {}", lucentTerm.getTerminal().getName());
                    } else {
                        log.warn("TerminalConnection is not Lucent-compatible: {}", term);
                    }
                }
            }
        }
    }

    public void unholdCall(String callId) throws Exception {
        CallMetadata metadata = activeCalls.get(callId);
        if (metadata == null) throw new IllegalArgumentException("Call not found");

        Call call = metadata.getCall();
        Connection[] connections = call.getConnections();

        for (Connection conn : connections) {
            TerminalConnection[] terminals = conn.getTerminalConnections();
            if (terminals != null) {
                for (TerminalConnection term : terminals) {
                    if (term instanceof LucentTerminalConnection lucentTerm) {
                        lucentTerm.unhold();

                        log.info("Call placed on hold at terminal: {}", lucentTerm.getTerminal().getName());
                    } else {
                        log.warn("TerminalConnection is not Lucent-compatible: {}", term);
                    }
                }
            }
        }
    }

    public void terminateCall(String callId) throws Exception {
        CallMetadata metadata = activeCalls.get(callId);
        if (metadata == null) throw new IllegalArgumentException("Call not found");

        Call call = metadata.getCall();
        Connection[] connections = call.getConnections();

        if (connections != null) {
            for (Connection conn : connections) {
                try {
                    conn.disconnect();
                    log.info("Disconnected connection: {}", conn);
                } catch (Exception e) {
                    log.warn("Could not disconnect connection: {}", e.getMessage());
                }
            }
        } else {
            throw new IllegalStateException("No active connections for call " + callId);
        }
    }

    public void addToConference(String callId, String newParticipantExt) throws Exception {
        CallMetadata metadata = activeCalls.get(callId);
        if (metadata == null) throw new IllegalArgumentException("Call not found");

        Call call = metadata.getCall();

        if (call instanceof LucentCall lucentCall) {
            Address participant = provider.getAddress(newParticipantExt);

            log.info("✅ Extension {} added to conference on call {}", newParticipantExt, callId);
        } else {
            throw new UnsupportedOperationException("Call does not support conference");
        }
    }

    public Map<String, CallMetadata> getActiveCalls() {
        return activeCalls;
    }

    public Provider getProvider() {
        return provider;
    }
}
