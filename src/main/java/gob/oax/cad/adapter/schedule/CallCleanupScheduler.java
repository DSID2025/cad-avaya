package gob.oax.cad.adapter.schedule;

import gob.oax.cad.adapter.listener.CallMonitoringService;
import gob.oax.cad.adapter.model.CallMetadata;
import gob.oax.cad.adapter.model.CallState;
import gob.oax.cad.adapter.model.CallStreamEvent;
import gob.oax.cad.adapter.model.EventSource;
import gob.oax.cad.adapter.util.CallUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.telephony.Call;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class CallCleanupScheduler {

    private final CallMonitoringService callMonitoringService;
    private final Consumer<CallStreamEvent> callEventConsumer;

    // Run every 60 seconds
    @Scheduled(fixedDelay = 60000)
    public void cleanupOrphanCalls() {
        Instant now = Instant.now();
        int cleaned = 0;

        Iterator<Map.Entry<String, CallMetadata>> iterator = callMonitoringService.getActiveCalls().entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, CallMetadata> entry = iterator.next();
            String callId = entry.getKey();
            CallMetadata meta = entry.getValue();
            Duration age = Duration.between(meta.getCreatedAt(), now);

            if (age.toSeconds() > 120) { // Threshold: 2 min
                iterator.remove();
                cleaned++;

                log.warn("Orphan call [{}] removed after {} seconds", callId, age.getSeconds());

                Call call = meta.getCall();
                String from = CallUtils.extractFrom(call);

                // Optionally emit a synthetic disconnected event
                CallStreamEvent event = new CallStreamEvent(
                        callId,
                        null,
                        from,
                        "",
                        CallState.DISCONNECTED,
                        false,
                        now,
                        EventSource.ADAPTER,
                        "Call removed due to timeout (orphan)"
                );
                callEventConsumer.accept(event);
            }
        }

        if (cleaned > 0) {
            log.info("Cleanup complete. Removed {} orphan calls", cleaned);
        }
    }

}
