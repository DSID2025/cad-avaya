package gob.oax.cad.adapter.controller;

import gob.oax.cad.adapter.listener.CallMonitoringService;
import gob.oax.cad.adapter.model.RouteCallRequest;
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

    private final CallMonitoringService callMonitoringService;

    @PostMapping("/route")
    public ResponseEntity<?> routeCall(@RequestBody RouteCallRequest request) {
        try {
            callMonitoringService.routeCall(request.getCallId(), request.getTerminal());

            return ResponseEntity.ok("✅ Call successfully routed to terminal: " + request.getTerminal());
        } catch (Exception e) {
            log.error("Failed to route call: {}", e.getMessage(), e);

            return ResponseEntity.internalServerError().body("Failed to route call: " + e.getMessage());
        }
    }
}

