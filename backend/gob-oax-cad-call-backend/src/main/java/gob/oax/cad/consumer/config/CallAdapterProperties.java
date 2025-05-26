package gob.oax.cad.consumer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "adapter.avaya")
public class CallAdapterProperties {
    private String url;
    private Integer maxRetries;
}
