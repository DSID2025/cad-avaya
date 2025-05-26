package gob.oax.cad.adapter.controller;

import gob.oax.cad.adapter.dto.LocationDataResponse;
import gob.oax.cad.adapter.service.LocationLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationLookupController {

    private final LocationLookupService locationLookupService;

    @GetMapping("/lookup")
    public ResponseEntity<LocationDataResponse> lookupLocationByPhoneNumber(@RequestParam String number) {
        LocationDataResponse response = locationLookupService.lookupLocationByPhoneNumber(number);
        return ResponseEntity.ok(response);
    }
}