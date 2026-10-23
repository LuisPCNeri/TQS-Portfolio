package ua.pt.tqs.HW1_125264.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import ua.pt.tqs.HW1_125264.Entities.Compartment;
import ua.pt.tqs.HW1_125264.Entities.Parcel.ParcelSize;

@Repository
public interface CompartmentRepository extends JpaRepository<Compartment, Long> {

    /* @Lock with PESSIMISTIC_WRITE prevents two requests grabbing the same locker */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT c FROM Compartment c
        WHERE c.station.id = :stationId
        AND c.size = :size
        AND c.status = 'AVAILABLE'
        ORDER BY c.id
        LIMIT 1
        """)
    Optional<Compartment> findFirstAvailable(
        @Param("stationId") Long stationId,
        @Param("size") ParcelSize size
    );

}
