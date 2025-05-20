package gob.oax.cad.webhook.adapter;

import gob.oax.cad.webhook.config.JtapiProperties;
import gob.oax.cad.webhook.model.CallStreamEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.telephony.Address;
import javax.telephony.Call;
import javax.telephony.CallObserver;
import javax.telephony.JtapiPeer;
import javax.telephony.JtapiPeerFactory;
import javax.telephony.Provider;
import javax.telephony.Terminal;
import javax.telephony.events.CallEv;
import javax.telephony.events.ConnAlertingEv;
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
    private final Map<String, Call> activeCalls = new ConcurrentHashMap<>();

    /**
     * Inicializa el listener JTAPI al arrancar el componente.
     * <p>
     * Establece la conexión con el proveedor JTAPI usando las credenciales configuradas,
     * obtiene el terminal y registra los listeners necesarios para monitorear eventos de llamadas.
     * </p>
     */
    @PostConstruct
    public void init() {
        try {
            // Create JTAPI provider connection string
            String credentials = String.format("%s;login=%s;passwd=%s",
                    properties.getProvider(),
                    properties.getLogin(),
                    properties.getPassword());

            // Connect to JTAPI provider
            JtapiPeer peer = JtapiPeerFactory.getJtapiPeer(null);
            // Nos conectamos al proveedor JTAPI
            provider = peer.getProvider(credentials);

            // Observe provider state
            provider.addProviderListener(new ProviderListenerAdapter());

            // Observe incoming calls on the main entry point (trunk or IVR line)
            Address entryAddress = provider.getAddress(properties.getDevice());

            entryAddress.addCallObserver(new CallObserver() {
                @Override
                public void callChangedEvent(CallEv[] events) {
                    for (CallEv ev : events) {
                        if (ev instanceof ConnAlertingEv) {
                            Call call = ev.getCall();
                            String callId = call.toString();

                            // Register and listen to this specific call
                            activeCalls.put(callId, call);

                            call.addCallListener(new CallLifecycleListener(callEventConsumer, activeCalls));

                            log.info("Incoming call [{}] detected on address [{}]", callId, entryAddress.getName());
                        }
                    }
                }
            });

            log.info("✅ JTAPI adapter is now listening for calls on address [{}]", entryAddress.getName());

        } catch (Exception e) {
            log.error("Error al inicializar JTAPI (Listener): {}", e.getMessage(), e);
        }
    }

    // Exposed to backend: route the call to an agent's terminal
    public void routeCall(String callId, String targetExtension) throws Exception {
        Call call = activeCalls.get(callId);

        if (call == null) {
            log.warn("No active call found with ID: {}", callId);
            throw new IllegalArgumentException("Call not found: " + callId);
        }

        Terminal terminal = provider.getTerminal(properties.getDevice());
        Address address = provider.getAddress(properties.getDevice());

        try {
            log.info("Connecting call [{}] to extension [{}]", callId, targetExtension);

            call.connect(terminal, address, targetExtension);

            log.info("Call [{}] successfully routed to [{}]", callId, targetExtension);

            activeCalls.remove(callId);
        } catch (Exception e) {
            log.error("Error routing call {} to {}: {}", callId, targetExtension, e.getMessage(), e);
            throw e;
        }
    }
}
}
