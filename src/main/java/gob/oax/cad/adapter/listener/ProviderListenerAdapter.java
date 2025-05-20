package gob.oax.cad.adapter.listener;

import lombok.extern.slf4j.Slf4j;

import javax.telephony.ProviderEvent;
import javax.telephony.ProviderListener;

@Slf4j
public class ProviderListenerAdapter implements ProviderListener {

    @Override
    public void providerInService(ProviderEvent providerEvent) {
        log.info("Provider in service: {}", providerEvent);
    }

    @Override
    public void providerEventTransmissionEnded(ProviderEvent providerEvent) {
        log.info("Provider event transmission ended: {}", providerEvent);
    }

    @Override
    public void providerOutOfService(ProviderEvent providerEvent) {
        log.info("Provider out of service: {}", providerEvent);
    }

    @Override
    public void providerShutdown(ProviderEvent providerEvent) {
        log.info("Provider shutdown: {}", providerEvent);
    }
}