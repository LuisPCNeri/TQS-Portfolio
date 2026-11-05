package ua.pt.tqs.HW1_125264.Controllers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ua.pt.tqs.HW1_125264.DTOs.LockerStationDTO;
import ua.pt.tqs.HW1_125264.Entities.LockerStation;
import ua.pt.tqs.HW1_125264.Entities.Parcel;
import ua.pt.tqs.HW1_125264.Repositories.CompartmentRepository;
import ua.pt.tqs.HW1_125264.Services.StationService;

@RestController
@RequestMapping("/api/v1/stations")
@CrossOrigin(origins = "http://localhost:3000")
public class StationController {

    private final StationService stationService;
    private final CompartmentRepository compartmentRepository;

    public StationController(StationService stationService, CompartmentRepository compartmentRepository) {
        this.stationService        = stationService;
        this.compartmentRepository = compartmentRepository;
    }

    private LockerStationDTO tDto(LockerStation s) {
        Map<String, Long> free = compartmentRepository
        .countAvailableBySize(s.getId())
        .stream()
        .collect(Collectors.toMap(
            row -> ((Parcel.ParcelSize) row[0]).name(),
            row -> (Long) row[1]
        ));

        return LockerStationDTO.builder()
            .id(s.getId())
            .name(s.getName())
            .addr(s.getAddr())
            .latitude(s.getLatitude())
            .longitude(s.getLongitude())
            .freeCompartmentsBySize(free)
            .build();
    }

    @GetMapping
    public List<LockerStationDTO> findNearestStations(@RequestParam String addr) {
        return stationService.findNearestStations(addr)
            .stream().map(this::tDto).toList();
    }

}
