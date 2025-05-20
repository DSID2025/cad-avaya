package gob.oax.cad.adapter.listener;

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
import javax.telephony.JtapiPeer;
import javax.telephony.JtapiPeerFactory;
import javax.telephony.Provider;
import javax.telephony.Terminal;
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

            // Connect to JTAPI provider
            JtapiPeer peer = JtapiPeerFactory.getJtapiPeer(null);
            // Nos conectamos al proveedor JTAPI
            provider = peer.getProvider(credentials);
            // Agregamos un listener para el proveedor
            provider.addProviderListener(new ProviderListenerAdapter());

            Address entryAddress = provider.getAddress(properties.getDevice());

            entryAddress.addCallObserver(new CallObserver() {
                @Override
                public void callChangedEvent(CallEv[] events) {
                    try {
                        for (CallEv ev : events) {
                            if (ev instanceof ConnAlertingEv) {
                                Call call = ev.getCall();
                                String callId = call.toString();

                                // Registrar la llamada en el mapa de llamadas activas si no existe
                                activeCalls.putIfAbsent(callId, new CallMetadata(call, Instant.now()));

                                call.addCallListener(new CallLifecycleListener(callEventConsumer, activeCalls));

                                log.info("Incoming call [{}] detected on address [{}]", callId, entryAddress.getName());
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

    public Map<String, CallMetadata> getActiveCalls() {
        return activeCalls;
    }

    public Provider getProvider() {
        return provider;
    }
}
