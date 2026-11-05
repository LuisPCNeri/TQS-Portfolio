package ua.pt.tqs.HW1_125264.Controllers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ua.pt.tqs.HW1_125264.Entities.LockerStation;
import ua.pt.tqs.HW1_125264.Repositories.CompartmentRepository;
import ua.pt.tqs.HW1_125264.Services.StationService;

@ExtendWith(MockitoExtension.class)
class StationControllerTest {

    @Mock private StationService stationService;
    @Mock private CompartmentRepository compartmentRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(new StationController(stationService, compartmentRepository))
            .build();
    }

    @Test
    void findNearestStationsReturnsStations() throws Exception {
        LockerStation station = mock(LockerStation.class);
        when(station.getId()).thenReturn(1L);
        when(station.getName()).thenReturn("Porto Central");
        when(station.getAddr()).thenReturn("Rua de Santa Catarina 100, Porto");
        when(station.getLatitude()).thenReturn(41.1496);
        when(station.getLongitude()).thenReturn(-8.6109);
        when(stationService.findNearestStations("Porto")).thenReturn(List.of(station));
        when(compartmentRepository.countAvailableBySize(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/stations").param("addr", "Porto"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Porto Central"))
            .andExpect(jsonPath("$[0].latitude").value(41.1496));
    }

    @Test
    void missingAddressReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/stations"))
            .andExpect(status().isBadRequest());
    }
}
