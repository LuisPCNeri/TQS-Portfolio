package ua.pt.tqs.HW1_125264;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import ua.pt.tqs.HW1_125264.DTOs.ParcelDTO;
import ua.pt.tqs.HW1_125264.Entities.Compartment;
import ua.pt.tqs.HW1_125264.Entities.Parcel;
import ua.pt.tqs.HW1_125264.Entities.Compartment.CompartmentStatus;
import ua.pt.tqs.HW1_125264.Entities.Parcel.ParcelSize;
import ua.pt.tqs.HW1_125264.Entities.Parcel.ParcelState;
import ua.pt.tqs.HW1_125264.Exceptions.HardwareFailureException;
import ua.pt.tqs.HW1_125264.Exceptions.InvalidPickupCodeException;
import ua.pt.tqs.HW1_125264.Exceptions.NoAvailableCompartmentException;
import ua.pt.tqs.HW1_125264.Exceptions.ParcelNotFoundException;
import ua.pt.tqs.HW1_125264.LockerHardwareSim.InMemoryLockerHardwareFake;
import ua.pt.tqs.HW1_125264.Repositories.CompartmentRepository;
import ua.pt.tqs.HW1_125264.Repositories.ParcelRepository;
import ua.pt.tqs.HW1_125264.Services.ParcelService;

import org.junit.jupiter.api.BeforeEach;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class ParcelServiceIT {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("tuttibox_test")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url",      postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired ParcelService parcelService;
    @Autowired CompartmentRepository compartmentRepository;
    @Autowired ParcelRepository parcelRepository;
    @Autowired InMemoryLockerHardwareFake hardwareFake;
    @Autowired JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        jdbcTemplate.execute(
            "TRUNCATE TABLE parcel_history, parcel, compartment, locker_station RESTART IDENTITY CASCADE");
        new ResourceDatabasePopulator(new ClassPathResource("data.sql"))
            .execute(jdbcTemplate.getDataSource());

        hardwareFake.reset();
    }

    @Test
    void testCreateParcelReservesCompartment() {
        ParcelDTO dto = new ParcelDTO();
        dto.setSize(ParcelSize.M);
        dto.setRecipientContact("user@domain.com");

        Parcel parcel = parcelService.createParcel(1L, dto);

        assertNotNull(parcel.getTrackingCode());
        assertEquals(ParcelState.CREATED, parcel.getState());
        assertNotNull(parcel.getPickupDeadline());
    }

    @Test
    void testDropOffTransitionsToReadyForPickup() {
        // create first
        ParcelDTO dto = new ParcelDTO();
        dto.setSize(ParcelSize.M);
        dto.setRecipientContact("user@domain.com");
        Parcel parcel = parcelService.createParcel(1L, dto);

        // drop off
        Parcel dropped = parcelService.dropOffParcel(
            parcel.getTrackingCode(), "Courier", LocalDateTime.now());

        assertEquals(ParcelState.READY_FOR_PICKUP, dropped.getState());
        assertNotNull(dropped.getPickupCode());
    }

    @Test
    void testPickUpFreesCompartment() {
        ParcelDTO dto = new ParcelDTO();
        dto.setSize(ParcelSize.M);
        dto.setRecipientContact("user@domain.com");
        Parcel parcel = parcelService.createParcel(1L, dto);
        Long compartmentId = parcel.getCompartment().getId();
        Parcel dropped = parcelService.dropOffParcel(
            parcel.getTrackingCode(), "Courier", LocalDateTime.now());

        parcelService.pickupParcel(
            dropped.getTrackingCode(),
            dropped.getPickupCode(),
            "Recipient",
            LocalDateTime.now()
        );

        Compartment c = compartmentRepository
            .findById(compartmentId).orElseThrow();
        assertEquals(CompartmentStatus.AVAILABLE, c.getStatus());

        Parcel picked = parcelRepository
            .findByTrackingCode(dropped.getTrackingCode()).orElseThrow();
        assertNull(picked.getCompartment());
    }

    @Test
    void testFreedCompartmentCanBeReusedAfterPickup() {
        ParcelDTO dto = new ParcelDTO();
        dto.setSize(ParcelSize.M);
        dto.setRecipientContact("user@domain.com");

        Parcel first = parcelService.createParcel(1L, dto);
        Long compartmentId = first.getCompartment().getId();
        Parcel dropped = parcelService.dropOffParcel(
            first.getTrackingCode(), "Courier", LocalDateTime.now());
        parcelService.pickupParcel(
            dropped.getTrackingCode(), dropped.getPickupCode(), "Recipient", LocalDateTime.now());

        Parcel second = parcelService.createParcel(1L, dto);

        assertEquals(compartmentId, second.getCompartment().getId());
    }

    @Test
    void testConcurrentAllocationGivesDifferentCompartments() throws Exception {
        ParcelDTO dto = new ParcelDTO();
        dto.setSize(ParcelSize.M);
        dto.setRecipientContact("user@domain.com");

        var results   = new CopyOnWriteArrayList<Parcel>();
        var failures  = new CopyOnWriteArrayList<Exception>();
        var latch     = new CountDownLatch(1);
        var threads   = new ArrayList<Thread>();

        for (int i = 0; i < 2; i++) {
            threads.add(Thread.ofVirtual().start(() -> {
                try {
                    latch.await();
                    results.add(parcelService.createParcel(1L, dto));
                } catch (Exception e) {
                    failures.add(e);
                }
            }));
        }

        latch.countDown();
        for (var t : threads) t.join();

        assertTrue(failures.isEmpty(), () -> "Concurrent allocation failed: " + failures);
        assertEquals(2, results.size());

        // compartments must all be different
        var compartmentIds = results.stream()
            .map(p -> p.getCompartment().getId())
            .toList();
        assertEquals(2, new HashSet<>(compartmentIds).size());
    }

    @Test
    void testHardwareFailureOnDropOffDoesNotTransitionState() {
        ParcelDTO dto = new ParcelDTO();
        dto.setSize(ParcelSize.M);
        dto.setRecipientContact("user@domain.com");
        Parcel parcel = parcelService.createParcel(1L, dto);

        hardwareFake.setGlobalFailure(true);

        assertThrows(HardwareFailureException.class, () ->
            parcelService.dropOffParcel(
                parcel.getTrackingCode(), "Courier", LocalDateTime.now()));

        Parcel unchanged = parcelRepository
            .findByTrackingCode(parcel.getTrackingCode()).orElseThrow();
        assertEquals(ParcelState.CREATED, unchanged.getState());
    }

    @Test
    void testCreateParcelWithoutAvailableCompartmentThrows() {
        ParcelDTO dto = new ParcelDTO();
        dto.setSize(ParcelSize.M);
        dto.setRecipientContact("user@domain.com");

        var ex = assertThrows(NoAvailableCompartmentException.class,
            () -> parcelService.createParcel(999L, dto));

        assertTrue(ex.getMessage().contains("station with id 999"));
    }

    @Test
    void testDropOffUnknownParcelThrows() {
        assertThrows(ParcelNotFoundException.class,
            () -> parcelService.dropOffParcel(
                UUID.randomUUID(), "Courier", LocalDateTime.now()));
    }

    @Test
    void testPickupUnknownParcelThrows() {
        assertThrows(ParcelNotFoundException.class,
            () -> parcelService.pickupParcel(
                UUID.randomUUID(), UUID.randomUUID(), "Recipient", LocalDateTime.now()));
    }

    @Test
    void testPickupWithInvalidCodeThrows() {
        Parcel parcel = createAndDropOffParcel();

        assertThrows(InvalidPickupCodeException.class,
            () -> parcelService.pickupParcel(
                parcel.getTrackingCode(), UUID.randomUUID(), "Recipient", LocalDateTime.now()));
    }

    @Test
    void testPickupWhenNotReadyThrows() {
        Parcel parcel = createAndDropOffParcel();
        parcelService.pickupParcel(
            parcel.getTrackingCode(), parcel.getPickupCode(), "Recipient", LocalDateTime.now());

        assertThrows(IllegalStateException.class,
            () -> parcelService.pickupParcel(
                parcel.getTrackingCode(), parcel.getPickupCode(), "Recipient", LocalDateTime.now()));
    }

    @Test
    void testPickupHardwareFailureThrows() {
        Parcel parcel = createAndDropOffParcel();
        hardwareFake.setGlobalFailure(true);

        assertThrows(HardwareFailureException.class,
            () -> parcelService.pickupParcel(
                parcel.getTrackingCode(), parcel.getPickupCode(), "Recipient", LocalDateTime.now()));
    }

    @Test
    void testGetParcelByTrackingCodeNotFound() {
        assertThrows(ParcelNotFoundException.class,
            () -> parcelService.getParcelByTrackingCode(UUID.randomUUID()));
    }

    @Test
    void testGetParcelByTrackingCodeReturnsParcel() {
        Parcel parcel = createParcel();

        assertEquals(parcel.getTrackingCode(),
            parcelService.getParcelByTrackingCode(parcel.getTrackingCode()).getTrackingCode());
    }

    @Test
    void testGetAllParcelsAndFilters() {
        Parcel parcel = createParcel();

        assertTrue(parcelService.getAllParcels().stream()
            .anyMatch(p -> p.getTrackingCode().equals(parcel.getTrackingCode())));
        assertTrue(parcelService.getParcelsByStation("Porto Central").stream()
            .anyMatch(p -> p.getTrackingCode().equals(parcel.getTrackingCode())));
        assertTrue(parcelService.getParcelsByState(ParcelState.CREATED).stream()
            .anyMatch(p -> p.getTrackingCode().equals(parcel.getTrackingCode())));
    }

    @Test
    void testMarkAsReturnedNotFound() {
        assertThrows(ParcelNotFoundException.class,
            () -> parcelService.markAsReturned(UUID.randomUUID(), LocalDateTime.now()));
    }

    @Test
    void testMarkAsReturnedUpdatesState() {
        Parcel parcel = createAndDropOffParcel();
        parcel.setState(ParcelState.EXPIRED);
        parcelRepository.save(parcel);

        Parcel returned = parcelService.markAsReturned(
            parcel.getTrackingCode(), LocalDateTime.now());

        assertEquals(ParcelState.RETURNED, returned.getState());
    }

    @Test
    void testMarkAsReturnedFreesCompartment() {
        Parcel parcel = createAndDropOffParcel();
        Long compartmentId = parcel.getCompartment().getId();
        parcel.setState(ParcelState.EXPIRED);
        parcelRepository.save(parcel);

        Parcel returned = parcelService.markAsReturned(
            parcel.getTrackingCode(), LocalDateTime.now());

        Compartment c = compartmentRepository.findById(compartmentId).orElseThrow();
        assertEquals(CompartmentStatus.AVAILABLE, c.getStatus());

        Parcel reloaded = parcelRepository
            .findByTrackingCode(returned.getTrackingCode()).orElseThrow();
        assertNull(reloaded.getCompartment());
    }

    private Parcel createParcel() {
        ParcelDTO dto = new ParcelDTO();
        dto.setSize(ParcelSize.M);
        dto.setRecipientContact("user@domain.com");
        return parcelService.createParcel(1L, dto);
    }

    private Parcel createAndDropOffParcel() {
        Parcel parcel = createParcel();
        return parcelService.dropOffParcel(
            parcel.getTrackingCode(), "Courier", LocalDateTime.now());
    }
}
