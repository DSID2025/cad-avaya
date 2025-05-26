package gob.oax.cad.consumer.controller;

import gob.oax.cad.consumer.model.PhoneLookupRequest;
import gob.oax.cad.consumer.model.LocationDataResponse;
import gob.oax.cad.consumer.service.CallLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {

    private final CallLocationService callLocationService;

    @PostMapping("/lookup")
    public ResponseEntity<LocationDataResponse> lookup(@RequestBody PhoneLookupRequest request) {
        return ResponseEntity.ok(callLocationService.lookupLocation(request.getPhoneNumber()));
    }
}
