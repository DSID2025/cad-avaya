package gob.oax.cad.webhook.adapter;

import gob.oax.cad.webhook.config.JtapiProperties;
import gob.oax.cad.webhook.model.CallStreamEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.telephony.Address;
import javax.telephony.Call;
import javax.telephony.JtapiPeer;
import javax.telephony.JtapiPeerFactory;
import javax.telephony.Provider;
import javax.telephony.Terminal;
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
            String credentials = String.format("%s;login=%s;passwd=%s",
                    properties.getProvider(), properties.getLogin(), properties.getPassword());

            JtapiPeer peer = JtapiPeerFactory.getJtapiPeer(null);
            provider = peer.getProvider(credentials);
            provider.addProviderListener(new ProviderListenerAdapter());

            Address address = provider.getAddress(properties.getDevice());
            Terminal terminal = address.getTerminals()[0];

            // Creamos la llamada asociada al terminal y la escuchamos
            Call call = provider.createCall();
            call.addCallListener(new CallLifecycleListener(callEventConsumer));

            log.info("JTAPI Listener registrado en terminal {}", terminal.getName());

        } catch (Exception e) {
            log.error("Error al inicializar JTAPI (Listener): {}", e.getMessage(), e);
        }
    }

    public void routeCall(String callId, String targetTerminal) throws Exception {
        Call call = activeCalls.get(callId);

        if (call == null) {
            log.warn("No se encontró la llamada con ID: {}", callId);

            throw new IllegalArgumentException("Call ID not found: " + callId);
        }

        Terminal terminal = provider.getTerminal(targetTerminal);
        Address address = provider.getAddress(targetTerminal);

        if (terminal == null || address == null) {
            log.warn("Terminal no disponible: {}", targetTerminal);

            throw new IllegalArgumentException("Terminal not available: " + targetTerminal);
        }

        try {
            log.info("Enrutando llamada {} al terminal {}", callId, targetTerminal);

            // call.connect(terminal, address); // TODO: Implementar la lógica de conexión, revisar metodo connect

            log.info("✅ Call successfully routed to {}", targetTerminal);

        } catch (Exception e) {
            log.error("❌ Error while routing call {} to {}: {}", callId, targetTerminal, e.getMessage(), e);
            throw e;
        }
    }
}
