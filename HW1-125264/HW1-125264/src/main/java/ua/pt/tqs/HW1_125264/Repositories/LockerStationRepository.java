package ua.pt.tqs.HW1_125264.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ua.pt.tqs.HW1_125264.Entities.LockerStation;

@Repository
public interface LockerStationRepository extends JpaRepository<LockerStation, Long> {

}
