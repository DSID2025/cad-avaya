package gob.oax.cad.consumer.config;

import gob.oax.cad.consumer.model.LocationDataResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "locationAdapterClient",
        url = "${adapter.rapidsos.url}"
)
public interface LocationAdapterClient {

    @GetMapping("/api/location/lookup")
    LocationDataResponse lookupLocation(@RequestParam("number") String phoneNumber);
}
