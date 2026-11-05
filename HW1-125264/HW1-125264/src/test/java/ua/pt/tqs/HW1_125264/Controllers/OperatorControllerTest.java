package ua.pt.tqs.HW1_125264.Controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ua.pt.tqs.HW1_125264.Entities.Parcel;
import ua.pt.tqs.HW1_125264.Entities.Parcel.ParcelSize;
import ua.pt.tqs.HW1_125264.Entities.Parcel.ParcelState;
import ua.pt.tqs.HW1_125264.Exceptions.ParcelNotFoundException;
import ua.pt.tqs.HW1_125264.Services.ParcelService;

@ExtendWith(MockitoExtension.class)
class OperatorControllerTest {

    @Mock private ParcelService parcelService;

    private MockMvc mockMvc;
    private final Clock clock = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(new OperatorController(parcelService, clock))
            .build();
    }

    private Parcel parcel(UUID trackingCode) {
        Parcel p = new Parcel(
            "Porto Central", ParcelSize.M, "user@domain.com", LocalDateTime.now(clock));
        p.setTrackingCode(trackingCode);
        return p;
    }

    @Test
    void getAllParcelsReturnsList() throws Exception {
        UUID trackingCode = UUID.randomUUID();
        when(parcelService.getAllParcels()).thenReturn(List.of(parcel(trackingCode)));

        mockMvc.perform(get("/api/v1/operator/parcels"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].trackingCode").value(trackingCode.toString()));
    }

    @Test
    void getExpiredParcelsReturnsList() throws Exception {
        UUID trackingCode = UUID.randomUUID();
        Parcel expired = parcel(trackingCode);
        expired.setState(ParcelState.EXPIRED);
        when(parcelService.getParcelsByState(ParcelState.EXPIRED))
            .thenReturn(List.of(expired));

        mockMvc.perform(get("/api/v1/operator/parcels/expired"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].state").value("EXPIRED"));
    }

    @Test
    void returnParcelReturnsOk() throws Exception {
        UUID trackingCode = UUID.randomUUID();
        when(parcelService.markAsReturned(eq(trackingCode), any()))
            .thenReturn(parcel(trackingCode));

        mockMvc.perform(put("/api/v1/operator/parcels/{trackingCode}/return", trackingCode))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.trackingCode").value(trackingCode.toString()));
    }

    @Test
    void returnUnknownParcelReturnsNotFound() throws Exception {
        UUID trackingCode = UUID.randomUUID();
        when(parcelService.markAsReturned(eq(trackingCode), any()))
            .thenThrow(new ParcelNotFoundException("missing"));

        mockMvc.perform(put("/api/v1/operator/parcels/{trackingCode}/return", trackingCode))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("missing"));
    }
}
