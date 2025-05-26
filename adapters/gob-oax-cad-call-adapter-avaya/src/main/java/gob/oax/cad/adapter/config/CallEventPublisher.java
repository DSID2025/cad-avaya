package gob.oax.cad.adapter.config;

import gob.oax.cad.adapter.model.CallStreamEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.function.Consumer;

/**
 * Configuración para la publicación y consumo de eventos de llamadas usando Reactor.
 * <p>
 * Expone un {@link Flux} para suscribirse a los eventos de llamadas y un {@link Consumer}
 * como bean de Spring para emitir nuevos eventos en el flujo.
 * </p>
 */
@Configuration
public class CallEventPublisher {

    /**
     * Sink Reactor para emitir y propagar eventos de llamadas a múltiples suscriptores.
     */
    private final Sinks.Many<CallStreamEvent> sink = Sinks.many().multicast().onBackpressureBuffer();

    /**
     * Obtiene un flujo reactivo de los eventos de llamadas emitidos.
     *
     * @return un {@link Flux} de {@link CallStreamEvent}
     */
    public Flux<CallStreamEvent> getEvents() {
        return sink.asFlux();
    }

    /**
     * Bean de Spring que expone un consumidor para emitir eventos de llamadas al flujo.
     *
     * @return un {@link Consumer} de {@link CallStreamEvent}
     */
    @Bean
    public Consumer<CallStreamEvent> callEventConsumer() {
        return sink::tryEmitNext;
    }
}