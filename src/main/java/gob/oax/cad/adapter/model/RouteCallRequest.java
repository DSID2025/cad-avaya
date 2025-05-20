package gob.oax.cad.webhook.model;

import lombok.Data;

@Data
public class RouteCallRequest {
    private String callId;
    private String terminal;
}
