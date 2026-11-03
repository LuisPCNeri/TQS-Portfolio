package ua.pt.tqs.HW1_125264.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.pt.tqs.HW1_125264.Entities.LockerStation;
import ua.pt.tqs.HW1_125264.Exceptions.GeocodingException;
import ua.pt.tqs.HW1_125264.GeoCoding.IGeoCodingPort;
import ua.pt.tqs.HW1_125264.GeoCoding.IGeoCodingPort.Coordinates;
import ua.pt.tqs.HW1_125264.Repositories.LockerStationRepository;

@ExtendWith(MockitoExtension.class)
class StationServiceTest {

    @Mock private LockerStationRepository lockerStationRepository;
    @Mock private IGeoCodingPort geoCodingPort;
    @InjectMocks private StationService stationService;

    private LockerStation station(double lat, double lon) {
        LockerStation s = mock(LockerStation.class);
        when(s.getLatitude()).thenReturn(lat);
        when(s.getLongitude()).thenReturn(lon);
        return s;
    }

    @Test
    void findNearestStationsSortsByDistance() {
        LockerStation near = station(0.0, 0.0);
        LockerStation far = station(0.0, 1.0);
        when(geoCodingPort.geocode("origin")).thenReturn(new Coordinates(0.0, 0.0));
        when(lockerStationRepository.findAll()).thenReturn(List.of(far, near));

        List<LockerStation> result = stationService.findNearestStations("origin");

        assertEquals(List.of(near, far), result);
    }

    @Test
    void findNearestStationsPropagatesGeocodingFailure() {
        when(geoCodingPort.geocode("bad"))
            .thenThrow(new GeocodingException("geocoding failed"));

        assertThrows(GeocodingException.class,
            () -> stationService.findNearestStations("bad"));
    }

    @Test
    void findNearestStationsReturnsEmptyWhenNoStationsExist() {
        when(geoCodingPort.geocode("empty")).thenReturn(new Coordinates(0.0, 0.0));
        when(lockerStationRepository.findAll()).thenReturn(List.of());

        assertTrue(stationService.findNearestStations("empty").isEmpty());
    }
}
