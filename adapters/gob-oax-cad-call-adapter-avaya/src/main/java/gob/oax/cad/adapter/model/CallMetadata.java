package gob.oax.cad.adapter.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.telephony.Call;
import java.time.Instant;

@Data
@AllArgsConstructor
public class CallMetadata {
    private Call call;
    private Instant createdAt;

}
