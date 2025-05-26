package gob.oax.cad.adapter.dto;

import lombok.Data;

@Data
public class LocationDataResponse {
    private String phoneNumber;
    private String location;
    private String confidence;
    private Boolean found;
    private String message;
}
