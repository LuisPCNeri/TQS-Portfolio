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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ua.pt.tqs.HW1_125264.DTOs.CompartmentDTO;
import ua.pt.tqs.HW1_125264.DTOs.LockerStationDTO;
import ua.pt.tqs.HW1_125264.DTOs.ParcelDTO;
import ua.pt.tqs.HW1_125264.Entities.Compartment;
import ua.pt.tqs.HW1_125264.Entities.LockerStation;
import ua.pt.tqs.HW1_125264.Entities.Parcel;
import ua.pt.tqs.HW1_125264.Entities.Parcel.ParcelState;
import ua.pt.tqs.HW1_125264.Exceptions.CompartmentNotFoundException;
import ua.pt.tqs.HW1_125264.Exceptions.ParcelNotFoundException;
import ua.pt.tqs.HW1_125264.Repositories.LockerStationRepository;
import ua.pt.tqs.HW1_125264.Services.CompartmentService;
import ua.pt.tqs.HW1_125264.Services.ParcelService;

@RestController
@RequestMapping("/api/v1/operator")
@CrossOrigin(origins = "http://localhost:3000")
public class OperatorController {

    private ParcelService parcelService;
    private CompartmentService compartmentService;
    private LockerStationRepository lockerStationRepository;
    private Clock clock;

    public OperatorController(ParcelService parcelService, CompartmentService compartmentService,
            LockerStationRepository lockerStationRepository, Clock clock) {
        this.parcelService          = parcelService;
        this.compartmentService     = compartmentService;
        this.lockerStationRepository = lockerStationRepository;
        this.clock                  = clock;
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

    private CompartmentDTO toDto(Compartment compartment) {
        return CompartmentDTO.builder()
            .id(compartment.getId())
            .size(compartment.getSize())
            .status(compartment.getStatus())
            .build();
    }

    private LockerStationDTO toDto(LockerStation station) {
        return LockerStationDTO.builder()
            .id(station.getId())
            .name(station.getName())
            .addr(station.getAddr())
            .latitude(station.getLatitude())
            .longitude(station.getLongitude())
            .build();
    }

    @GetMapping("/stations")
    public List<LockerStationDTO> getStations() {
        return lockerStationRepository.findAll().stream().map(this::toDto).toList();
    }

    @GetMapping("/stations/{stationId}/compartments")
    public List<CompartmentDTO> getCompartments(@PathVariable Long stationId) {
        return compartmentService.getCompartmentsByStation(stationId)
            .stream().map(this::toDto).toList();
    }

    @PutMapping("/compartments/{compartmentId}/out-of-service")
    public ResponseEntity<CompartmentDTO> markCompartmentOutOfService(
            @PathVariable Long compartmentId) {
        Compartment compartment = compartmentService.markOutOfService(compartmentId);
        return ResponseEntity.ok(toDto(compartment));
    }

    @GetMapping("/parcels")
    public List<ParcelDTO> getParcels(
            @RequestParam(required = false) String station,
            @RequestParam(required = false) ParcelState state) {

        List<Parcel> parcels;

        if (station != null && state != null) {
            parcels = parcelService.getParcelsByStationAndState(station, state);
        } else if (station != null) {
            parcels = parcelService.getParcelsByStation(station);
        } else if (state != null) {
            parcels = parcelService.getParcelsByState(state);
        } else {
            parcels = parcelService.getAllParcels();
        }

        return parcels.stream().map(this::toDto).toList();
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

    @ExceptionHandler(CompartmentNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCompartmentNotFound(CompartmentNotFoundException e) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException e) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("error", e.getMessage()));
    }

}
