package gob.oax.cad.adapter.health;

import gob.oax.cad.adapter.listener.CallMonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.telephony.Provider;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProviderHealthIndicator implements HealthIndicator {

    private final CallMonitoringService callMonitoringService;

    @Override
    public Health health() {
        try {
            log.info("Checking JTAPI provider health");

            Instant now = Instant.now();
            Provider provider = callMonitoringService.getProvider();

            if (provider == null) {
                log.warn("JTAPI Provider not initialized yet");
                return Health.down()
                        .withDetail("state", "NOT_INITIALIZED")
                        .withDetail("checkedAt", now)
                        .build();
            }


            int state = provider.getState();
            return switch (state) {
                case Provider.IN_SERVICE -> Health.up()
                        .withDetail("state", "IN_SERVICE")
                        .withDetail("checkedAt", now)
                        .build();
                case Provider.OUT_OF_SERVICE -> Health.down()
                        .withDetail("state", "OUT_OF_SERVICE")
                        .withDetail("checkedAt", now)
                        .build();
                case Provider.SHUTDOWN -> Health.down()
                        .withDetail("state", "SHUTDOWN")
                        .withDetail("checkedAt", now)
                        .build();
                default -> Health.unknown()
                        .withDetail("state", state)
                        .withDetail("checkedAt", now)
                        .build();
            };

        } catch (Exception e) {
            log.error("Error checking JTAPI provider health: {}", e.getMessage(), e);
            return Health.down(e).build();
        }
    }
}
