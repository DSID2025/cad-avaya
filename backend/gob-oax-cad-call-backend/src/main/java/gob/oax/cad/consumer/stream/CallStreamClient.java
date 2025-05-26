package gob.oax.cad.consumer.stream;

import gob.oax.cad.consumer.config.CallAdapterProperties;
import gob.oax.cad.consumer.model.CallStreamEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class CallStreamClient {

    private final CallAdapterProperties properties;
    private final CallEventHandlerService handler;

    private WebClient webClient;

    @EventListener(ContextRefreshedEvent.class)
    public void subscribeToEvents() {
        webClient = WebClient.builder()
                .baseUrl(properties.getUrl())
                .build();

        webClient.get()
                .uri("/api/calls/events-stream")
                .retrieve()
                .bodyToFlux(CallStreamEvent.class)
                .doOnSubscribe(sub -> log.info("Suscrito al stream de llamadas desde {}", properties.getUrl()))
                .doOnNext(handler::handleEvent)
                .doOnError(err -> log.error("Error recibiendo eventos: {}", err.getMessage()))
                .retryWhen(Retry.backoff(properties.getMaxRetries(), Duration.ofSeconds(10)).doAfterRetry(retrySignal ->
                        log.warn("Reintentando conexión con adapter... intento {}", retrySignal.totalRetries() + 1)
                ))
                .onErrorResume(err -> {
                    log.warn("Conexión fallida. Flux retenido.");
                    return Flux.never();
                })
                .subscribe();

    }
}
