package ua.pt.tqs.HW1_125264.Controllers;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ua.pt.tqs.HW1_125264.DTOs.LockerStationDTO;
import ua.pt.tqs.HW1_125264.Entities.LockerStation;
import ua.pt.tqs.HW1_125264.Services.StationService;

@RestController
@RequestMapping("/api/v1/stations")
@CrossOrigin(origins = "*")
public class StationController {

    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    private LockerStationDTO tDto(LockerStation s) {
        return LockerStationDTO.builder()
            .id(s.getId())
            .name(s.getName())
            .addr(s.getAddr())
            .latitude(s.getLatitude())
            .longitude(s.getLongitude())
            .build();
    }

    @GetMapping
    public List<LockerStationDTO> findNearestStations(@RequestParam String addr) {
        return stationService.findNearestStations(addr)
            .stream().map(this::tDto).toList();
    }

}
