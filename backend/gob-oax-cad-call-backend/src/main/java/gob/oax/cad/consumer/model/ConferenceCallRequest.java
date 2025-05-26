package gob.oax.cad.consumer.model;

import lombok.Data;

@Data
public class ConferenceCallRequest {
    private String callId;
    private String newParticipantExtension;
}