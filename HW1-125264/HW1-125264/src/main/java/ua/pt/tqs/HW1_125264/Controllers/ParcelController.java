package ua.pt.tqs.HW1_125264.Controllers;


import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ua.pt.tqs.HW1_125264.DTOs.ParcelDTO;
import ua.pt.tqs.HW1_125264.Entities.Parcel;
import ua.pt.tqs.HW1_125264.Exceptions.HardwareFailureException;
import ua.pt.tqs.HW1_125264.Exceptions.NoAvailableCompartmentException;
import ua.pt.tqs.HW1_125264.Exceptions.ParcelNotFoundException;
import ua.pt.tqs.HW1_125264.Services.ParcelService;

@RestController
@RequestMapping("/api/v1/parcels")
@CrossOrigin(origins = "*")
public class ParcelController {

    private ParcelService parcelService;
    private Clock clock;

    public ParcelController(ParcelService parcelService, Clock clock) {
        this.parcelService = parcelService;
        this.clock         = clock;
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
                .pickupDeadline(createdParcel.getPickupDeadline())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(parcelResp);
    }

    @PutMapping("/{trackingCode}/dropoff")
    public ResponseEntity<ParcelDTO> dropoffParcel(@PathVariable UUID trackingCode, @RequestParam String actor) {
        Parcel createdParcel = parcelService.dropOffParcel(trackingCode, actor, LocalDateTime.now(clock));

        ParcelDTO parcelResp = ParcelDTO.builder().trackingCode(createdParcel.getTrackingCode())
                .station(createdParcel.getStation())
                .recipientContact(createdParcel.getRecipientContact())
                .size(createdParcel.getSize())
                .state(createdParcel.getState())
                .createdAt(createdParcel.getCreatedAt())
                .readyForPickupAt(createdParcel.getReadyForPickupAt())
                .pickupDeadline(createdParcel.getPickupDeadline())
                .build();

        return ResponseEntity.ok(parcelResp);
    }

    @ExceptionHandler(ParcelNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleParcelNotFound(ParcelNotFoundException e) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(HardwareFailureException.class)
    public ResponseEntity<Map<String, String>> handleHardwareFailure(HardwareFailureException e) {

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(NoAvailableCompartmentException.class)
    public ResponseEntity<Map<String, String>> handleNoCompartment(NoAvailableCompartmentException e) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("error", e.getMessage()));
    }

}
