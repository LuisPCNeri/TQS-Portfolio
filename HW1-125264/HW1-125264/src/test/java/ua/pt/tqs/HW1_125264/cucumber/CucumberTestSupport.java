package ua.pt.tqs.HW1_125264.cucumber;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import ua.pt.tqs.HW1_125264.DTOs.ParcelDTO;
import ua.pt.tqs.HW1_125264.Entities.Compartment;
import ua.pt.tqs.HW1_125264.Entities.Compartment.CompartmentStatus;
import ua.pt.tqs.HW1_125264.Entities.LockerStation;
import ua.pt.tqs.HW1_125264.Entities.Parcel;
import ua.pt.tqs.HW1_125264.Entities.Parcel.ParcelSize;
import ua.pt.tqs.HW1_125264.Entities.Parcel.ParcelState;
import ua.pt.tqs.HW1_125264.Repositories.CompartmentRepository;
import ua.pt.tqs.HW1_125264.Repositories.LockerStationRepository;
import ua.pt.tqs.HW1_125264.Repositories.ParcelRepository;
import ua.pt.tqs.HW1_125264.Schedulers.ParcelExpiryScheduler;
import ua.pt.tqs.HW1_125264.Services.CompartmentService;
import ua.pt.tqs.HW1_125264.Services.ParcelService;

@Component
public class CucumberTestSupport {

    private final LockerStationRepository stationRepository;
    private final CompartmentRepository compartmentRepository;
    private final ParcelRepository parcelRepository;
    private final ParcelService parcelService;
    private final CompartmentService compartmentService;
    private final ParcelExpiryScheduler expiryScheduler;
    private final Clock clock;

    public CucumberTestSupport(
            LockerStationRepository stationRepository,
            CompartmentRepository compartmentRepository,
            ParcelRepository parcelRepository,
            ParcelService parcelService,
            CompartmentService compartmentService,
            ParcelExpiryScheduler expiryScheduler,
            Clock clock) {
        this.stationRepository = stationRepository;
        this.compartmentRepository = compartmentRepository;
        this.parcelRepository = parcelRepository;
        this.parcelService = parcelService;
        this.compartmentService = compartmentService;
        this.expiryScheduler = expiryScheduler;
        this.clock = clock;
    }

    public LockerStation station(String name) {
        return stationRepository.findAll().stream()
            .filter(s -> s.getName().equals(name))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Station not found: " + name));
    }

    public Parcel registerParcel(String size, String recipient, String stationName) {
        ParcelDTO dto = new ParcelDTO();
        dto.setSize(ParcelSize.valueOf(size));
        dto.setRecipientContact(recipient);
        return parcelService.createParcel(station(stationName).getId(), dto);
    }

    public Parcel dropOff(Parcel parcel) {
        return parcelService.dropOffParcel(parcel.getTrackingCode(), "Courier", now());
    }

    public Parcel readyForPickup(String stationName) {
        return dropOff(registerParcel("M", "recipient@example.com", stationName));
    }

    public long availableCompartments(String stationName, String size) {
        ParcelSize parcelSize = ParcelSize.valueOf(size);
        return compartmentRepository.countAvailableBySize(station(stationName).getId())
            .stream()
            .filter(row -> row[0] == parcelSize)
            .map(row -> (Long) row[1])
            .findFirst()
            .orElse(0L);
    }

    public Compartment firstAvailableCompartment(String stationName) {
        return compartmentRepository.findByStationIdOrderByIdAsc(station(stationName).getId())
            .stream()
            .filter(c -> c.getStatus() == CompartmentStatus.AVAILABLE)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "No available compartment at " + stationName));
    }

    public Compartment compartment(Long compartmentId) {
        return compartmentRepository.findById(compartmentId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Compartment not found: " + compartmentId));
    }

    public Parcel reload(Parcel parcel) {
        return parcelService.getParcelByTrackingCode(parcel.getTrackingCode());
    }

    public List<Parcel> parcelsAt(String stationName, ParcelState state) {
        return parcelService.getParcelsByStationAndState(stationName, state);
    }

    public void occupyAllCompartments(String stationName, String size) {
        ParcelSize parcelSize = ParcelSize.valueOf(size);
        List<Compartment> compartments =
            compartmentRepository.findByStationIdOrderByIdAsc(station(stationName).getId());
        compartments.stream()
            .filter(c -> c.getSize() == parcelSize && c.getStatus() == CompartmentStatus.AVAILABLE)
            .forEach(c -> c.setStatus(CompartmentStatus.OCCUPIED));
        compartmentRepository.saveAll(compartments);
    }

    public void passPickupDeadline(Parcel parcel) {
        parcel.setPickupDeadline(now().minusHours(1));
        parcelRepository.save(parcel);
    }

    public void runExpiryCheck() {
        expiryScheduler.expireOverdueParcels();
    }

    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
