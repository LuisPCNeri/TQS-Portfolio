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
import ua.pt.tqs.HW1_125264.Exceptions.InvalidPickupCodeException;
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

    private ParcelDTO toDto(Parcel parcel) {
        return ParcelDTO.builder().trackingCode(parcel.getTrackingCode())
                .station(parcel.getStation())
                .recipientContact(parcel.getRecipientContact())
                .size(parcel.getSize())
                .state(parcel.getState())
                .createdAt(parcel.getCreatedAt())
                .readyForPickupAt(parcel.getReadyForPickupAt())
                .pickupDeadline(parcel.getPickupDeadline())
                .build();
    }

    @PostMapping
    public ResponseEntity<ParcelDTO> createParcel(@RequestParam Long stationId, @RequestBody ParcelDTO parcel) {
        Parcel createdParcel = parcelService.createParcel(stationId, parcel);

        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(createdParcel));
    }

    @PutMapping("/{trackingCode}/dropoff")
    public ResponseEntity<ParcelDTO> dropoffParcel(@PathVariable UUID trackingCode, @RequestParam String actor) {
        Parcel createdParcel = parcelService.dropOffParcel(trackingCode, actor, LocalDateTime.now(clock));

        return ResponseEntity.ok(toDto(createdParcel));
    }

    @PutMapping("/{trackingCode}/pickup")
    public ResponseEntity<ParcelDTO> pickupParcel(@PathVariable UUID trackingCode, @RequestParam UUID pickupCode, @RequestParam String actor) {
        Parcel parcel = parcelService.pickupParcel(trackingCode, pickupCode, actor, LocalDateTime.now(clock));
        return ResponseEntity.ok(toDto(parcel));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String,String>> handleIllegalState(IllegalStateException e) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(InvalidPickupCodeException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPickupCode(InvalidPickupCodeException e) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("error", e.getMessage()));
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
