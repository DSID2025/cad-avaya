package gob.oax.cad.webhook.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "avaya.jtapi")
public class JtapiProperties {
    private String provider;
    private String login;
    private String password;
    private String device;
}