package gob.oax.cad.adapter.service;

import gob.oax.cad.adapter.client.LocationProviderClient;
import gob.oax.cad.adapter.dto.LocationDataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationLookupService {

    private final LocationProviderClient locationProviderClient;

    public LocationDataResponse lookupLocationByPhoneNumber(String phoneNumber) {
        log.info("Se recibió solicitud de ubicación para el número: {}", phoneNumber);

        try {
            LocationDataResponse response = locationProviderClient.lookupLocationByPhoneNumber(phoneNumber);

            boolean found = response.getLocation() != null;
            response.setFound(found);

            String message = found ? "Ubicación encontrada" : "No se encontró ubicación";
            response.setMessage(message);

            if (response.getLocation() != null) {
                log.info("Ubicación encontrada para el número {}: {}", phoneNumber, response.getLocation());
            } else {
                log.warn("No se encontró ubicación para el número: {}", phoneNumber);
            }

            return response;

        } catch (Exception ex) {
            log.error("Error al consultar ubicación para el número {}: {}", phoneNumber, ex.getMessage(), ex);
            throw ex;
        }
    }
}