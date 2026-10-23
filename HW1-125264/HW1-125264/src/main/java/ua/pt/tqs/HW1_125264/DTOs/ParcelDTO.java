package ua.pt.tqs.HW1_125264.DTOs;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.pt.tqs.HW1_125264.Entities.Parcel.ParcelSize;
import ua.pt.tqs.HW1_125264.Entities.Parcel.ParcelState;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParcelDTO {
    @JsonProperty(access = Access.READ_ONLY)
    private UUID trackingCode;

    private String station;
    private String recipientContact;
    private ParcelSize size;

    @JsonProperty(access = Access.READ_ONLY)
    private ParcelState state;
    @JsonProperty(access = Access.READ_ONLY)
    private LocalDateTime createdAt;
    @JsonProperty(access = Access.READ_ONLY)
    private LocalDateTime pickupDeadline;
    @JsonProperty(access = Access.READ_ONLY)
    private LocalDateTime readyForPickupAt;
}
