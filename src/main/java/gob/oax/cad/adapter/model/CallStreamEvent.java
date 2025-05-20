package gob.oax.cad.adapter.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class CallStreamEvent {
    private String callId;
    private String from;               // número origen
    private String to;                 // terminal asignado (si aplica)
    private CallState state;          // Enum: RINGING, CONNECTED, etc.
    private boolean pendingAssignment;
    private Instant timestamp;        // Fecha y hora del evento
    private EventSource source;       // Enum: ADAPTER, TEST, MANUAL, etc.
    private String notes;             // (opcional) para anotaciones o comentarios
}