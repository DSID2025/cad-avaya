package gob.oax.cad.adapter.model;

import lombok.Data;

@Data
public class RouteCallRequest {
    private String callId;
    private String agentId;
    private String terminal;
}
