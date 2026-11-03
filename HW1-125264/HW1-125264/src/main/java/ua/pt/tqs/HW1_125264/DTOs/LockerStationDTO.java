package ua.pt.tqs.HW1_125264.DTOs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor @NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LockerStationDTO {

    private Long id;
    private String name;
    private String addr;
    private double latitude;
    private double longitude;

}
