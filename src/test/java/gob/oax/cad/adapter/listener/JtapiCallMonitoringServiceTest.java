package gob.oax.cad.adapter.listener;

import gob.oax.cad.adapter.config.JtapiProperties;
import gob.oax.cad.adapter.listener.impl.JtapiCallMonitoringService;
import gob.oax.cad.adapter.model.CallMetadata;
import gob.oax.cad.adapter.model.CallStreamEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.telephony.*;
import java.time.Instant;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JtapiCallMonitoringServiceTest {

    private JtapiProperties properties;
    private Consumer<CallStreamEvent> callEventConsumer;
    private JtapiCallMonitoringService service;

    @BeforeEach
    void setUp() {
        properties = mock(JtapiProperties.class);
        callEventConsumer = mock(Consumer.class);
        service = new JtapiCallMonitoringService(properties, callEventConsumer);
    }

    @Test
    void testGetActiveCallsInitiallyEmpty() {
        Map<String, CallMetadata> activeCalls = service.getActiveCalls();
        assertNotNull(activeCalls);
        assertTrue(activeCalls.isEmpty());
    }

    @Test
    void testGetProviderInitiallyNull() {
        assertNull(service.getProvider());
    }

    @Test
    void testRouteCallThrowsIfCallNotFound() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            service.routeCall("nonexistent", "1001");
        });
        assertTrue(ex.getMessage().contains("Call not found"));
    }

    @Test
    void testRouteCallThrowsIfOriginTerminalOrAddressNull() throws Exception {
        // Arrange
        String callId = "call1";
        Call call = mock(Call.class);
        CallMetadata metadata = new CallMetadata(call, Instant.now());
        service.getActiveCalls().put(callId, metadata);

        Provider provider = mock(Provider.class);
        when(properties.getDevice()).thenReturn("device1");
        when(provider.getTerminal("device1")).thenReturn(null);
        when(provider.getAddress("device1")).thenReturn(null);

        // Reflection to set private provider field
        java.lang.reflect.Field providerField = JtapiCallMonitoringService.class.getDeclaredField("provider");
        providerField.setAccessible(true);
        providerField.set(service, provider);

        Exception ex = assertThrows(IllegalStateException.class, () -> {
            service.routeCall(callId, "1002");
        });
        assertTrue(ex.getMessage().contains("Origin terminal or address not found"));
    }

    @Test
    void testRouteCallSuccess() throws Exception {
        // Arrange
        String callId = "call2";
        Call call = mock(Call.class);
        CallMetadata metadata = new CallMetadata(call, Instant.now());
        service.getActiveCalls().put(callId, metadata);

        Provider provider = mock(Provider.class);
        Terminal terminal = mock(Terminal.class);
        Address address = mock(Address.class);

        when(properties.getDevice()).thenReturn("device2");
        when(provider.getTerminal("device2")).thenReturn(terminal);
        when(provider.getAddress("device2")).thenReturn(address);

        java.lang.reflect.Field providerField = JtapiCallMonitoringService.class.getDeclaredField("provider");
        providerField.setAccessible(true);
        providerField.set(service, provider);

        // Act
        service.routeCall(callId, "1003");

        // Assert
        verify(call).connect(terminal, address, "1003");
        assertFalse(service.getActiveCalls().containsKey(callId));
    }

    @Test
    void testRouteCallLogsAndThrowsOnConnectException() throws Exception {
        String callId = "call3";
        Call call = mock(Call.class);
        CallMetadata metadata = new CallMetadata(call, Instant.now());
        service.getActiveCalls().put(callId, metadata);

        Provider provider = mock(Provider.class);
        Terminal terminal = mock(Terminal.class);
        Address address = mock(Address.class);

        when(properties.getDevice()).thenReturn("device3");
        when(provider.getTerminal("device3")).thenReturn(terminal);
        when(provider.getAddress("device3")).thenReturn(address);

        doThrow(new RuntimeException("connect failed")).when(call).connect(terminal, address, "1004");

        java.lang.reflect.Field providerField = JtapiCallMonitoringService.class.getDeclaredField("provider");
        providerField.setAccessible(true);
        providerField.set(service, provider);

        Exception ex = assertThrows(RuntimeException.class, () -> {
            service.routeCall(callId, "1004");
        });
        assertEquals("connect failed", ex.getMessage());
        assertTrue(service.getActiveCalls().containsKey(callId));
    }
}
