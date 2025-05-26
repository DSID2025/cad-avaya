package gob.oax.cad.consumer.service;

import gob.oax.cad.consumer.config.LocationAdapterClient;
import gob.oax.cad.consumer.model.PhoneLookupRequest;
import gob.oax.cad.consumer.model.LocationDataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallLocationService {

    private final LocationAdapterClient locationAdapterClient;

    public LocationDataResponse lookupLocation(String phoneNumber) {
        log.info("Se recibió solicitud de ubicación para el número: {}", phoneNumber);

        try {
            PhoneLookupRequest lookupRequest = new PhoneLookupRequest(phoneNumber);
            LocationDataResponse result = locationAdapterClient.lookupLocation(lookupRequest.getPhoneNumber());

            if (result.getFound()) {
                log.info("Ubicación encontrada: {}", result);
            } else {
                log.warn("No se encontró ubicación para el número: {}", phoneNumber);
            }

            return result;

        } catch (Exception ex) {
            log.error("Error al consultar ubicación para el número {}: {}", phoneNumber, ex.getMessage(), ex);
            throw ex;
        }
    }
}
