package gob.oax.cad.webhook.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CallStreamEvent {
    private String eventType;
    private String callId;
    private String from;
    private String to;
}