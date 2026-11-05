package ua.pt.tqs.HW1_125264.Controllers;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ua.pt.tqs.HW1_125264.DTOs.ParcelDTO;
import ua.pt.tqs.HW1_125264.Entities.Parcel;
import ua.pt.tqs.HW1_125264.Entities.Parcel.ParcelState;
import ua.pt.tqs.HW1_125264.Exceptions.ParcelNotFoundException;
import ua.pt.tqs.HW1_125264.Services.ParcelService;

@RestController
@RequestMapping("/api/v1/operator")
@CrossOrigin(origins = "http://localhost:3000")
public class OperatorController {

    private ParcelService parcelService;
    private Clock clock;

    public OperatorController(ParcelService parcelService, Clock clock) {
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

    @GetMapping("/parcels")
    public List<ParcelDTO> getAllParcels() {
        return parcelService.getAllParcels().stream().map(this::toDto).toList();
    }

    @GetMapping("/parcels/expired")
    public List<ParcelDTO> getAllExpiredParcels() {
        return parcelService.getParcelsByState(ParcelState.EXPIRED)
            .stream().map(this::toDto).toList();
    }

    @PutMapping("/parcels/{trackingCode}/return")
    public ResponseEntity<ParcelDTO> returnParcel(@PathVariable UUID trackingCode) {
        Parcel parcel = parcelService.markAsReturned(trackingCode, LocalDateTime.now(clock));
        return ResponseEntity.ok(toDto(parcel));
    }

    @ExceptionHandler(ParcelNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleParcelNotFound(ParcelNotFoundException e) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", e.getMessage()));
    }

}
