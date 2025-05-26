package gob.oax.cad.adapter.controller;

import gob.oax.cad.adapter.listener.CallMonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.telephony.Provider;
import java.time.Instant;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/jtapi")
@RequiredArgsConstructor
public class JtapiAdminController {

    private final CallMonitoringService callMonitoringService;

    @PostMapping("/reconnect")
    public ResponseEntity<?> reconnectProvider() {
        boolean success = callMonitoringService.initialize();

        if (success) {
            return ResponseEntity.ok("JTAPI provider reconnected");
        } else {
            return ResponseEntity.status(500).body("Failed to reconnect JTAPI provider");
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getProviderStatus() {
        Provider provider = callMonitoringService.getProvider();
        String state;
        String code;

        if (provider == null) {
            state = "NOT_INITIALIZED";
            code = "NULL";
        } else {
            int providerState = provider.getState();
            state = switch (providerState) {
                case Provider.IN_SERVICE -> "IN_SERVICE";
                case Provider.OUT_OF_SERVICE -> "OUT_OF_SERVICE";
                case Provider.SHUTDOWN -> "SHUTDOWN";
                default -> "UNKNOWN";
            };
            code = String.valueOf(providerState);
        }

        return ResponseEntity.ok(Map.of(
                "status", state,
                "code", code,
                "checkedAt", Instant.now()
        ));
    }

}
