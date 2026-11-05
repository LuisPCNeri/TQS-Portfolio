package ua.pt.tqs.HW1_125264.Services;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import ua.pt.tqs.HW1_125264.Entities.Compartment;
import ua.pt.tqs.HW1_125264.Entities.Compartment.CompartmentStatus;
import ua.pt.tqs.HW1_125264.Exceptions.CompartmentNotFoundException;
import ua.pt.tqs.HW1_125264.Repositories.CompartmentRepository;

@Service
public class CompartmentService {

    private final CompartmentRepository compartmentRepository;

    public CompartmentService(CompartmentRepository compartmentRepository) {
        this.compartmentRepository = compartmentRepository;
    }

    public List<Compartment> getCompartmentsByStation(Long stationId) {
        return compartmentRepository.findByStationIdOrderByIdAsc(stationId);
    }

    @Transactional
    public Compartment markOutOfService(Long compartmentId) {
        Compartment compartment = compartmentRepository.findById(compartmentId)
            .orElseThrow(() -> new CompartmentNotFoundException(
                "Compartment not found with id: " + compartmentId));

        if (compartment.getStatus() != CompartmentStatus.AVAILABLE) {
            throw new IllegalStateException(
                "Only available compartments can be put out of service");
        }

        compartment.setStatus(CompartmentStatus.FAULTY);

        return compartmentRepository.save(compartment);
    }
}
