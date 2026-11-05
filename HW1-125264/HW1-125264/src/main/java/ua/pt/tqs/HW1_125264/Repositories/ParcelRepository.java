package ua.pt.tqs.HW1_125264.Repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.pt.tqs.HW1_125264.Entities.Parcel;
import ua.pt.tqs.HW1_125264.Entities.Parcel.ParcelState;

@Repository
public interface ParcelRepository extends JpaRepository<Parcel, Long> {

    List<Parcel> findByStateAndPickupDeadlineBefore(ParcelState state, LocalDateTime deadline);
    Optional<Parcel> findByTrackingCode(UUID trackingCode);
    List<Parcel> findByStation(String station);
    List<Parcel> findByState(ParcelState state);

    @Query("SELECT p FROM Parcel p LEFT JOIN FETCH p.history WHERE p.trackingCode = :trackingCode")
    Optional<Parcel> findByTrackingCodeWithHistory(@Param("trackingCode") UUID trackingCode);
}
