package gob.oax.cad.adapter.controller;

import gob.oax.cad.adapter.listener.JtapiCallMonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/calls")
@RequiredArgsConstructor
public class CallControlController {

    private final JtapiCallMonitoringService jtapiCallMonitoringService;

    @PostMapping("/{callId}/transfer")
    public ResponseEntity<?> transferCall(@PathVariable String callId, @RequestParam String toExtension) {
        try {
            jtapiCallMonitoringService.transferCall(callId, toExtension);
            return ResponseEntity.ok("Call transferred to " + toExtension);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Transfer failed: " + e.getMessage());
        }
    }

    @PostMapping("/{callId}/pause")
    public ResponseEntity<?> pauseCall(@PathVariable String callId) {
        try {
            jtapiCallMonitoringService.holdCall(callId);
            return ResponseEntity.ok("Call placed on hold");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Hold failed: " + e.getMessage());
        }
    }

    @PostMapping("/{callId}/resume")
    public ResponseEntity<?> resumeCall(@PathVariable String callId) {
        try {
            jtapiCallMonitoringService.unholdCall(callId);
            return ResponseEntity.ok("Call resumed");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Resume failed: " + e.getMessage());
        }
    }

    @PostMapping("/{callId}/end")
    public ResponseEntity<?> endCall(@PathVariable String callId) {
        try {
            jtapiCallMonitoringService.terminateCall(callId);
            return ResponseEntity.ok("Call disconnected");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("End call failed: " + e.getMessage());
        }
    }

}
