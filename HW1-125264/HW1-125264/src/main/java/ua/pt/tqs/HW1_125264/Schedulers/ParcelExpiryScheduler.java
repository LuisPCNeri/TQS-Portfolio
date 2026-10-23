package ua.pt.tqs.HW1_125264.Schedulers;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;
import ua.pt.tqs.HW1_125264.Entities.Parcel.ParcelState;
import ua.pt.tqs.HW1_125264.Repositories.ParcelRepository;

@Component
public class ParcelExpiryScheduler {

    private final ParcelRepository parcelRepository;
    private final Clock clock;

    public ParcelExpiryScheduler(ParcelRepository parcelRepository, Clock clock) {
        this.parcelRepository = parcelRepository;
        this.clock            = clock;
    }

    @Scheduled(fixedRateString = "${tuttibox.expiry-check-interval-ms:60000}")
    @Transactional
    public void expireOverdueParcels() {
        LocalDateTime now = LocalDateTime.now(clock);

        parcelRepository.findByStateAndPickupDeadlineBefore(
            ParcelState.READY_FOR_PICKUP, now
        ).forEach(p -> p.expire("System", now));

        parcelRepository.flush();
    }
}
