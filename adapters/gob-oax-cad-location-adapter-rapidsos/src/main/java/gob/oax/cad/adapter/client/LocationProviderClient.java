package gob.oax.cad.adapter.client;

import gob.oax.cad.adapter.config.FeignClientConfig;
import gob.oax.cad.adapter.dto.LocationDataResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "rapidSosClient",
        url = "${rapidsos.base-url}",
        configuration = FeignClientConfig.class
)
public interface LocationProviderClient {

    @GetMapping("/emergency-data")
    LocationDataResponse lookupLocationByPhoneNumber(@RequestParam("number") String phoneNumber);

}
