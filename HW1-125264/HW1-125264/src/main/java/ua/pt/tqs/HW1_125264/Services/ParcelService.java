package ua.pt.tqs.HW1_125264.Services;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import ua.pt.tqs.HW1_125264.DTOs.ParcelDTO;
import ua.pt.tqs.HW1_125264.Entities.Parcel;
import ua.pt.tqs.HW1_125264.Repositories.ParcelRepository;

@Service
public class ParcelService {

    private ParcelRepository parcelRepository;

    private Clock clock;

    public ParcelService(ParcelRepository parcelRepository, Clock clock) {
        this.parcelRepository = parcelRepository;
        this.clock = clock;
    }

    public Parcel createParcel(ParcelDTO parcel) {
        LocalDateTime now = LocalDateTime.now(clock);

        Parcel p = new Parcel(parcel.getStation(), parcel.getSize(), parcel.getRecipientContact(), now);
        return parcelRepository.save(p);
    }
}
