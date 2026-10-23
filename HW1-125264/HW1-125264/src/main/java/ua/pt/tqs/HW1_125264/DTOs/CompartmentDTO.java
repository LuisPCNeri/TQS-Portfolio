package ua.pt.tqs.HW1_125264.DTOs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.pt.tqs.HW1_125264.Entities.Compartment.CompartmentStatus;
import ua.pt.tqs.HW1_125264.Entities.Parcel.ParcelSize;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompartmentDTO {
    private Long id;
    private ParcelSize size;
    private CompartmentStatus status;
}
