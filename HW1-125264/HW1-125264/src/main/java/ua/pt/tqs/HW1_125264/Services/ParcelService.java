package ua.pt.tqs.HW1_125264.Services;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import ua.pt.tqs.HW1_125264.TuttiBoxProperties;
import ua.pt.tqs.HW1_125264.DTOs.ParcelDTO;
import ua.pt.tqs.HW1_125264.Entities.Compartment;
import ua.pt.tqs.HW1_125264.Entities.Parcel;
import ua.pt.tqs.HW1_125264.Entities.Compartment.CompartmentStatus;
import ua.pt.tqs.HW1_125264.Entities.Parcel.ParcelState;
import ua.pt.tqs.HW1_125264.Exceptions.HardwareFailureException;
import ua.pt.tqs.HW1_125264.Exceptions.InvalidPickupCodeException;
import ua.pt.tqs.HW1_125264.Exceptions.NoAvailableCompartmentException;
import ua.pt.tqs.HW1_125264.Exceptions.ParcelNotFoundException;
import ua.pt.tqs.HW1_125264.LockerHardwareSim.LockerHardwarePort;
import ua.pt.tqs.HW1_125264.LockerHardwareSim.LockerHardwarePort.OpenResult;
import ua.pt.tqs.HW1_125264.Repositories.CompartmentRepository;
import ua.pt.tqs.HW1_125264.Repositories.ParcelRepository;

@Service
public class ParcelService {

    private ParcelRepository parcelRepository;
    private CompartmentRepository compartmentRepository;
    private TuttiBoxProperties properties;
    private LockerHardwarePort hardwarePort;

    private Clock clock;

    public ParcelService(ParcelRepository parcelRepository, CompartmentRepository compartmentRepository, TuttiBoxProperties properties, Clock clock,
            LockerHardwarePort hardwarePort) {

        this.parcelRepository      = parcelRepository;
        this.compartmentRepository = compartmentRepository;
        this.properties            = properties;
        this.hardwarePort          = hardwarePort;

        this.clock = clock;
    }

    /* Random comment to be able to commit to see if workflow is running */

    @Transactional
    public Parcel createParcel(Long stationId, ParcelDTO parcel) {

        Compartment compartment = compartmentRepository.findFirstAvailable(stationId, parcel.getSize())
                .orElseThrow(() -> new NoAvailableCompartmentException(
            String.format("No available %s compartment at station with id %d", parcel.getSize(), stationId)
        ));

        compartment.setStatus(CompartmentStatus.OCCUPIED);
        compartmentRepository.save(compartment);

        LocalDateTime now = LocalDateTime.now(clock);

        Parcel p = new Parcel(compartment.getStation().getName(), parcel.getSize(), parcel.getRecipientContact(), now);
        p.setCompartment(compartment);

        LocalDateTime deadline = now.plusHours(properties.getPickupDeadlineHours());
        System.out.println("DEBUG pickup deadline hours: " + properties.getPickupDeadlineHours());
        System.out.println("DEBUG deadline: " + deadline);

        p.setPickupDeadline(deadline);
        System.out.println("DEBUG parcel pickup deadline after set: " + p.getPickupDeadline());

        return parcelRepository.save(p);
    }

    @Transactional
    public Parcel dropOffParcel(UUID trackingCode, String actor, LocalDateTime now) {
        Parcel parcel = parcelRepository.findByTrackingCode(trackingCode)
            .orElseThrow(() -> new ParcelNotFoundException("Parcel not found with tracking code: " + trackingCode));

        OpenResult result = hardwarePort.openCompartment(parcel.getCompartment().getId());
        if(result == OpenResult.HARDWARE_FAILURE) {
            throw new HardwareFailureException("Failed to open compartment for dropoff");
        }

        parcel.markAsInTransit(actor, now);
        parcel.markAsReadyForPickup(actor, now);

        return parcelRepository.save(parcel);
    }

    @Transactional
    public Parcel pickupParcel(UUID trackingCode, UUID pickupCode, String actor, LocalDateTime now) {

        Parcel parcel = parcelRepository.findByTrackingCode(trackingCode)
            .orElseThrow(() -> new ParcelNotFoundException("Parcel not found with tracking code: " + trackingCode));

        if(!parcel.getPickupCode().equals(pickupCode)) {
            throw new InvalidPickupCodeException("Invalid pickup code");
        }

        if(parcel.getState() != ParcelState.READY_FOR_PICKUP) {
            throw new IllegalStateException("Parcel is not ready for pickup");
        }

        OpenResult result = hardwarePort.openCompartment(parcel.getCompartment().getId());
        if(result == OpenResult.HARDWARE_FAILURE) {
            throw new HardwareFailureException("Failed to open compartment for dropoff");
        }

        parcel.markAsPickedUp(actor, now);

        Compartment compartment = parcel.getCompartment();
        compartment.setStatus(CompartmentStatus.AVAILABLE);
        compartmentRepository.save(compartment);

        return parcelRepository.save(parcel);
    }

    public Parcel getParcelByTrackingCode(UUID trackingCode) {
        return parcelRepository.findByTrackingCode(trackingCode)
            .orElseThrow(() -> new ParcelNotFoundException("Parcel not found with tracking code: " + trackingCode));
    }

    public List<Parcel> getAllParcels() {
        return parcelRepository.findAll();
    }

    public List<Parcel> getParcelsByStation(String station) {
        return parcelRepository.findByStation(station);
    }

    public List<Parcel> getParcelsByState(ParcelState state) {
        return parcelRepository.findByState(state);
    }

    @Transactional
    public Parcel markAsReturned(UUID trackingCode, LocalDateTime now) {

        Parcel parcel = parcelRepository.findByTrackingCode(trackingCode)
            .orElseThrow(() -> new ParcelNotFoundException("Parcel not found with tracking code: " + trackingCode));

        parcel.markAsReturned("Operator", now);

        Compartment compartment = parcel.getCompartment();
        compartment.setStatus(CompartmentStatus.OCCUPIED);
        compartmentRepository.save(compartment);

        return parcelRepository.save(parcel);
    }
}
