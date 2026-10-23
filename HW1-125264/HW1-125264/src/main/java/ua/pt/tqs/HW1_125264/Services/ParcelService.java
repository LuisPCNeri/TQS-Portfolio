package ua.pt.tqs.HW1_125264.Services;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import ua.pt.tqs.HW1_125264.TuttiBoxProperties;
import ua.pt.tqs.HW1_125264.DTOs.ParcelDTO;
import ua.pt.tqs.HW1_125264.Entities.Compartment;
import ua.pt.tqs.HW1_125264.Entities.Parcel;
import ua.pt.tqs.HW1_125264.Entities.Compartment.CompartmentStatus;
import ua.pt.tqs.HW1_125264.Exceptions.NoAvailableCompartmentException;
import ua.pt.tqs.HW1_125264.Repositories.CompartmentRepository;
import ua.pt.tqs.HW1_125264.Repositories.ParcelRepository;

@Service
public class ParcelService {

    private ParcelRepository parcelRepository;
    private CompartmentRepository compartmentRepository;
    private TuttiBoxProperties properties;

    private Clock clock;

    public ParcelService(ParcelRepository parcelRepository, CompartmentRepository compartmentRepository, TuttiBoxProperties properties, Clock clock) {
        this.parcelRepository      = parcelRepository;
        this.compartmentRepository = compartmentRepository;
        this.properties            = properties;

        this.clock = clock;
    }

    /* Random comment to be able to commit to see if workflow is running */

    @Transactional
    public Parcel createParcel(Long stationId, ParcelDTO parcel) {

        Compartment compartment = compartmentRepository.findFirstAvailable(stationId, parcel.getSize())
                .orElseThrow(() -> new NoAvailableCompartmentException(
            String.format("No available %s compartment at station with id %ld", parcel.getSize(), stationId)
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
}
