package ua.pt.tqs.HW1_125264.Services;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import ua.pt.tqs.HW1_125264.Entities.LockerStation;
import ua.pt.tqs.HW1_125264.GeoCoding.IGeoCodingPort;
import ua.pt.tqs.HW1_125264.GeoCoding.IGeoCodingPort.Coordinates;
import ua.pt.tqs.HW1_125264.Repositories.LockerStationRepository;

@Service
public class StationService {

    private LockerStationRepository lockerStationRepository;
    private IGeoCodingPort iGeoCodingPort;

    public StationService(LockerStationRepository lockerStationRepository, IGeoCodingPort iGeoCodingPort) {
        this.lockerStationRepository = lockerStationRepository;
        this.iGeoCodingPort          = iGeoCodingPort;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2) * Math.sin(dLon/2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }

    public List<LockerStation> findNearestStations(String address) {
        Coordinates coords = iGeoCodingPort.geocode(address);

        return lockerStationRepository.findAll().stream()
            .sorted(Comparator.comparingDouble(
                s -> haversine(coords.latitude(), coords.longitude(), s.getLatitude(), s.getLongitude()))
            )
            .toList();
    }
}
