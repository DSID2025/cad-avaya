package gob.oax.cad.consumer.model;

import lombok.Data;

@Data
public class TransferCallRequest {
    private String sourceCallId;
    private String targetExtension;
}
