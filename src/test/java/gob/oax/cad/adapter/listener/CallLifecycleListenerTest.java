package gob.oax.cad.adapter.listener;

import gob.oax.cad.adapter.model.CallMetadata;
import gob.oax.cad.adapter.model.CallState;
import gob.oax.cad.adapter.model.CallStreamEvent;
import gob.oax.cad.adapter.model.EventSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.telephony.Call;
import javax.telephony.CallEvent;
import javax.telephony.Connection;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CallLifecycleListenerTest {

    private Consumer<CallStreamEvent> callEventConsumer;
    private Map<String, CallMetadata> callRegistry;
    private CallLifecycleListener listener;

    @BeforeEach
    void setUp() {
        callEventConsumer = mock(Consumer.class);
        callRegistry = new HashMap<>();
        listener = new CallLifecycleListener(callEventConsumer, callRegistry);
    }

    @Test
    void testCallActiveRegistersCallAndEmitsEvent() {
        Call call = mock(Call.class);
        CallEvent event = mock(CallEvent.class);
        Connection connection = mock(Connection.class);

        when(event.getCall()).thenReturn(call);
        when(call.toString()).thenReturn("call-1");
        when(call.getConnections()).thenReturn(new Connection[]{connection});
        when(connection.getState()).thenReturn(Connection.CONNECTED);

        // Mock CallUtils.extractFrom/To
        mockStaticCallUtils("1001", "1002");

        listener.callActive(event);

        assertTrue(callRegistry.containsKey("call-1"));
        ArgumentCaptor<CallStreamEvent> captor = ArgumentCaptor.forClass(CallStreamEvent.class);
        verify(callEventConsumer).accept(captor.capture());
        CallStreamEvent evt = captor.getValue();
        assertEquals("call-1", evt.getCallId());
        assertEquals("1002", evt.getTo());
        assertEquals(CallState.CONNECTED, evt.getState());
        // assertFalse(evt.get());
        assertEquals(EventSource.ADAPTER, evt.getSource());
    }

    @Test
    void testCallActiveWithNoConnectionsDoesNothing() {
        Call call = mock(Call.class);
        CallEvent event = mock(CallEvent.class);

        when(event.getCall()).thenReturn(call);
        when(call.toString()).thenReturn("call-2");
        when(call.getConnections()).thenReturn(null);

        listener.callActive(event);

        assertTrue(callRegistry.containsKey("call-2"));
        verifyNoInteractions(callEventConsumer);
    }

    @Test
    void testCallActiveHandlesException() {
        CallEvent event = mock(CallEvent.class);
        when(event.getCall()).thenThrow(new RuntimeException("fail"));

        listener.callActive(event);

        // Should not throw
        verifyNoInteractions(callEventConsumer);
    }

    @Test
    void testCallInvalidEmitsEventAndRemovesFromRegistry() {
        Call call = mock(Call.class);
        CallEvent event = mock(CallEvent.class);

        when(event.getCall()).thenReturn(call);
        when(call.toString()).thenReturn("call-3");
        callRegistry.put("call-3", new CallMetadata(call, Instant.now()));

        listener.callInvalid(event);

        assertFalse(callRegistry.containsKey("call-3"));
        ArgumentCaptor<CallStreamEvent> captor = ArgumentCaptor.forClass(CallStreamEvent.class);
        verify(callEventConsumer).accept(captor.capture());
        CallStreamEvent evt = captor.getValue();
        assertEquals("call-3", evt.getCallId());
        assertEquals(CallState.DISCONNECTED, evt.getState());
        assertEquals(EventSource.ADAPTER, evt.getSource());
        assertEquals("Llamada inválida o terminada", evt.getNotes());
    }

    @Test
    void testCallInvalidHandlesException() {
        CallEvent event = mock(CallEvent.class);
        when(event.getCall()).thenThrow(new RuntimeException("fail"));

        listener.callInvalid(event);

        // Should not throw
        verifyNoInteractions(callEventConsumer);
    }

    @Test
    void testCallEventTransmissionEndedLogs() {
        Call call = mock(Call.class);
        CallEvent event = mock(CallEvent.class);
        when(event.getCall()).thenReturn(call);

        // Just ensure no exception is thrown
        listener.callEventTransmissionEnded(event);
    }

    // Utilidad para simular CallUtils.extractFrom/To
    private void mockStaticCallUtils(String from, String to) {
        try {
            var utilsClass = Class.forName("gob.oax.cad.adapter.util.CallUtils");
            var extractFrom = utilsClass.getDeclaredMethod("extractFrom", Call.class);
            var extractTo = utilsClass.getDeclaredMethod("extractTo", Call.class);

            // Usar Mockito para simular métodos estáticos (requiere mockito-inline en dependencias)
            mockStatic(utilsClass).when(() -> extractFrom.invoke(any())).thenReturn(from);
            mockStatic(utilsClass).when(() -> extractTo.invoke(any())).thenReturn(to);
        } catch (Exception ignored) {}
    }
}
