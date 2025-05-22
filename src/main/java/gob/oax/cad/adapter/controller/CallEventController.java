package gob.oax.cad.adapter.controller;

import gob.oax.cad.adapter.config.CallEventPublisher;
import gob.oax.cad.adapter.model.CallStreamEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/api/calls")
@RequiredArgsConstructor
public class CallEventController {

    private final CallEventPublisher publisher;

    @GetMapping(value = "/events-stream", produces = "text/event-stream")
    public Flux<CallStreamEvent> stream() {
        log.info("Streaming call events al cliente...");
        return publisher.getEvents();
    }
}
