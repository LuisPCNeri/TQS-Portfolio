package ua.pt.tqs.HW1_125264.Controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ua.pt.tqs.HW1_125264.Entities.Parcel;
import ua.pt.tqs.HW1_125264.Entities.Parcel.ParcelSize;
import ua.pt.tqs.HW1_125264.Exceptions.HardwareFailureException;
import ua.pt.tqs.HW1_125264.Exceptions.InvalidPickupCodeException;
import ua.pt.tqs.HW1_125264.Exceptions.NoAvailableCompartmentException;
import ua.pt.tqs.HW1_125264.Exceptions.ParcelNotFoundException;
import ua.pt.tqs.HW1_125264.Services.ParcelService;

@ExtendWith(MockitoExtension.class)
class ParcelControllerTest {

    @Mock private ParcelService parcelService;

    private MockMvc mockMvc;
    private final Clock clock = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(new ParcelController(parcelService, clock))
            .build();
    }

    private Parcel parcel(UUID trackingCode) {
        Parcel p = new Parcel(
            "Porto Central", ParcelSize.M, "user@domain.com", LocalDateTime.now(clock));
        p.setTrackingCode(trackingCode);
        p.setPickupDeadline(LocalDateTime.now(clock).plusHours(48));
        return p;
    }

    @Test
    void createParcelReturnsCreated() throws Exception {
        UUID trackingCode = UUID.randomUUID();
        when(parcelService.createParcel(eq(1L), any())).thenReturn(parcel(trackingCode));

        mockMvc.perform(post("/api/v1/parcels")
                .param("stationId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"size":"M","recipientContact":"user@domain.com"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.trackingCode").value(trackingCode.toString()))
            .andExpect(jsonPath("$.state").value("CREATED"));
    }

    @Test
    void createParcelWithoutCompartmentReturnsConflict() throws Exception {
        when(parcelService.createParcel(eq(1L), any()))
            .thenThrow(new NoAvailableCompartmentException(
                "No available M compartment at station with id 1"));

        mockMvc.perform(post("/api/v1/parcels")
                .param("stationId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"size":"M","recipientContact":"user@domain.com"}
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error")
                .value("No available M compartment at station with id 1"));
    }

    @Test
    void dropOffReturnsOk() throws Exception {
        UUID trackingCode = UUID.randomUUID();
        when(parcelService.dropOffParcel(eq(trackingCode), eq("Courier"), any()))
            .thenReturn(parcel(trackingCode));

        mockMvc.perform(put("/api/v1/parcels/{trackingCode}/dropoff", trackingCode)
                .param("actor", "Courier"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.trackingCode").value(trackingCode.toString()));
    }

    @Test
    void dropOffUnknownParcelReturnsNotFound() throws Exception {
        UUID trackingCode = UUID.randomUUID();
        when(parcelService.dropOffParcel(eq(trackingCode), eq("Courier"), any()))
            .thenThrow(new ParcelNotFoundException("missing"));

        mockMvc.perform(put("/api/v1/parcels/{trackingCode}/dropoff", trackingCode)
                .param("actor", "Courier"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("missing"));
    }

    @Test
    void dropOffHardwareFailureReturnsServiceUnavailable() throws Exception {
        UUID trackingCode = UUID.randomUUID();
        when(parcelService.dropOffParcel(eq(trackingCode), eq("Courier"), any()))
            .thenThrow(new HardwareFailureException("hardware"));

        mockMvc.perform(put("/api/v1/parcels/{trackingCode}/dropoff", trackingCode)
                .param("actor", "Courier"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.error").value("hardware"));
    }

    @Test
    void pickupReturnsOk() throws Exception {
        UUID trackingCode = UUID.randomUUID();
        when(parcelService.pickupParcel(eq(trackingCode), any(), eq("Recipient"), any()))
            .thenReturn(parcel(trackingCode));

        mockMvc.perform(put("/api/v1/parcels/{trackingCode}/pickup", trackingCode)
                .param("pickupCode", UUID.randomUUID().toString())
                .param("actor", "Recipient"))
            .andExpect(status().isOk());
    }

    @Test
    void pickupWithInvalidCodeReturnsForbidden() throws Exception {
        UUID trackingCode = UUID.randomUUID();
        when(parcelService.pickupParcel(eq(trackingCode), any(), eq("Recipient"), any()))
            .thenThrow(new InvalidPickupCodeException("invalid"));

        mockMvc.perform(put("/api/v1/parcels/{trackingCode}/pickup", trackingCode)
                .param("pickupCode", UUID.randomUUID().toString())
                .param("actor", "Recipient"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").value("invalid"));
    }

    @Test
    void pickupWhenNotReadyReturnsForbidden() throws Exception {
        UUID trackingCode = UUID.randomUUID();
        when(parcelService.pickupParcel(eq(trackingCode), any(), eq("Recipient"), any()))
            .thenThrow(new IllegalStateException("not ready"));

        mockMvc.perform(put("/api/v1/parcels/{trackingCode}/pickup", trackingCode)
                .param("pickupCode", UUID.randomUUID().toString())
                .param("actor", "Recipient"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").value("not ready"));
    }

    @Test
    void getParcelReturnsOk() throws Exception {
        UUID trackingCode = UUID.randomUUID();
        when(parcelService.getParcelByTrackingCode(trackingCode))
            .thenReturn(parcel(trackingCode));

        mockMvc.perform(get("/api/v1/parcels/{trackingCode}", trackingCode))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.trackingCode").value(trackingCode.toString()));
    }

    @Test
    void getUnknownParcelReturnsNotFound() throws Exception {
        UUID trackingCode = UUID.randomUUID();
        when(parcelService.getParcelByTrackingCode(trackingCode))
            .thenThrow(new ParcelNotFoundException("missing"));

        mockMvc.perform(get("/api/v1/parcels/{trackingCode}", trackingCode))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("missing"));
    }
}
