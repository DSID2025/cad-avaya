package gob.oax.cad.adapter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "rapidsos")
public class RapidSosClientProperties {
    private String baseUrl;
    private String authUrl;
    private String clientId;
    private String clientSecret;
}