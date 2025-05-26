package gob.oax.cad.consumer.model;


import lombok.Data;

@Data
public class AgentActionMessage {
    private String callId;
    private String action;
    private String agentId;
    private String timestamp;
}