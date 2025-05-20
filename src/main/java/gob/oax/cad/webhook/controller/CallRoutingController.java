package gob.oax.cad.webhook.controller;

import gob.oax.cad.webhook.adapter.JtapiCallMonitoringService;
import gob.oax.cad.webhook.model.RouteCallRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/calls")
@RequiredArgsConstructor
public class CallRoutingController {

    private final JtapiCallMonitoringService jtapiCallMonitoringService;

    @PostMapping("/route")
    public ResponseEntity<?> routeCall(@RequestBody RouteCallRequest request) {
        try {
            jtapiCallMonitoringService.routeCall(request.getCallId(), request.getTerminal());

            return ResponseEntity.ok("✅ Call successfully routed to terminal: " + request.getTerminal());
        } catch (Exception e) {
            log.error("Failed to route call: {}", e.getMessage(), e);

            return ResponseEntity.internalServerError().body("Failed to route call: " + e.getMessage());
        }
    }
}

