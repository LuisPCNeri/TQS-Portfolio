package ua.pt.tqs.HW1_125264.Controllers;


import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ua.pt.tqs.HW1_125264.DTOs.ParcelDTO;
import ua.pt.tqs.HW1_125264.Entities.Parcel;
import ua.pt.tqs.HW1_125264.Exceptions.NoAvailableCompartmentException;
import ua.pt.tqs.HW1_125264.Services.ParcelService;

@RestController
@RequestMapping("/api/v1/parcels")
@CrossOrigin(origins = "*")
public class ParcelController {

    private ParcelService parcelService;

    public ParcelController(ParcelService parcelService) {
        this.parcelService = parcelService;
    }

    @PostMapping
    public ResponseEntity<ParcelDTO> createParcel(@RequestParam Long stationId, @RequestBody ParcelDTO parcel) {
        Parcel createdParcel = parcelService.createParcel(stationId, parcel);

        ParcelDTO parcelResp = ParcelDTO.builder().trackingCode(createdParcel.getTrackingCode())
                .station(createdParcel.getStation())
                .recipientContact(createdParcel.getRecipientContact())
                .size(createdParcel.getSize())
                .state(createdParcel.getState())
                .createdAt(createdParcel.getCreatedAt())
                .readyForPickupAt(createdParcel.getReadyForPickupAt())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(parcelResp);
    }

    @ExceptionHandler(NoAvailableCompartmentException.class)
    public ResponseEntity<Map<String, String>> handleNoCompartment(NoAvailableCompartmentException e) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("error", e.getMessage()));
    }

}
