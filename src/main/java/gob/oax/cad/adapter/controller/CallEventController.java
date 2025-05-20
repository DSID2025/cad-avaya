package gob.oax.cad.adapter.controller;

import gob.oax.cad.adapter.config.CallEventPublisher;
import gob.oax.cad.adapter.model.CallStreamEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class CallEventController {

    private final CallEventPublisher publisher;

    @GetMapping(value = "/api/llamadas/eventos", produces = "text/event-stream")
    public Flux<CallStreamEvent> stream() {
        return publisher.getEvents();
    }
}
