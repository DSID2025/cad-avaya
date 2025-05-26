package gob.oax.cad.consumer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationDataResponse {
    private String phoneNumber;
    private String location;
    private String confidence;
    private Boolean found;
    private String message;
}