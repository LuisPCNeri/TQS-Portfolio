package ua.pt.tqs.HW1_125264.Entities;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ua.pt.tqs.HW1_125264.Entities.Parcel.ParcelSize;

class ParcelStateMachineTest {

    private Parcel parcel;
    private Clock clock = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);

    private static final Map<Parcel.ParcelState, Set<Parcel.ParcelState>> ALLOWED = Map.of(
        Parcel.ParcelState.CREATED, EnumSet.of(Parcel.ParcelState.CANCELLED, Parcel.ParcelState.IN_TRANSIT),
        Parcel.ParcelState.IN_TRANSIT, EnumSet.of(Parcel.ParcelState.READY_FOR_PICKUP, Parcel.ParcelState.DESTROYED),
        Parcel.ParcelState.READY_FOR_PICKUP, EnumSet.of(Parcel.ParcelState.PICKED_UP, Parcel.ParcelState.EXPIRED),
        Parcel.ParcelState.EXPIRED, EnumSet.of(Parcel.ParcelState.RETURNED),
        Parcel.ParcelState.PICKED_UP, EnumSet.noneOf(Parcel.ParcelState.class),
        Parcel.ParcelState.CANCELLED, EnumSet.noneOf(Parcel.ParcelState.class),
        Parcel.ParcelState.DESTROYED, EnumSet.noneOf(Parcel.ParcelState.class),
        Parcel.ParcelState.RETURNED, EnumSet.noneOf(Parcel.ParcelState.class)
    );

    @BeforeEach
    void setup() {
        parcel = new Parcel("Station A", ParcelSize.M, "user@domain.com", 
            LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC));
    }

    static Stream<Arguments> allTransitions() {
        var targets = Arrays.stream(Parcel.ParcelState.values())
            .filter(s -> s != Parcel.ParcelState.CREATED)
            .toList();

        return Arrays.stream(Parcel.ParcelState.values())
            .flatMap(from -> targets.stream()
                .map(to -> Arguments.of(from, to, ALLOWED.get(from).contains(to))));
    }

    private Runnable transitionTo(Parcel.ParcelState to) {
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        return switch (to) {
            case CANCELLED -> () -> parcel.cancel("Actor", now);
            case IN_TRANSIT -> () -> parcel.markAsInTransit("Actor", now);
            case DESTROYED -> () -> parcel.markAsDestroyed("Actor", now);
            case READY_FOR_PICKUP -> () -> parcel.markAsReadyForPickup("Actor", now);
            case PICKED_UP -> () -> parcel.markAsPickedUp("Actor", now);
            case EXPIRED -> () -> parcel.expire("Actor", now);
            case RETURNED -> () -> parcel.markAsReturned("Actor", now);
            case CREATED -> throw new IllegalArgumentException("No operation transitions to CREATED");
        };
    }

    @ParameterizedTest(name = "{0} -> {1} (allowed={2})")
    @MethodSource("allTransitions")
    void testAllStateTransitions(Parcel.ParcelState from, Parcel.ParcelState to, boolean allowed) {
        parcel.setState(from);
        Runnable transition = transitionTo(to);

        if (allowed) {
            transition.run();
            assertEquals(to, parcel.getState());
        } else {
            assertThrows(IllegalStateException.class, transition::run);
            assertEquals(from, parcel.getState());
        }
    }

    @Test
    void testInitialStateAndHistoryCreation() {
        assertEquals(Parcel.ParcelState.CREATED, parcel.getState());
        assertEquals(Parcel.ParcelSize.M, parcel.getSize());

        assertNotNull(parcel.getTrackingCode());
        assertNotNull(parcel.getCreatedAt());

        assertEquals(1, parcel.getHistory().size());
        assertEquals(Parcel.ParcelState.CREATED, parcel.getHistory().get(0).state());
        assertEquals("Partner", parcel.getHistory().get(0).actor());
    }

    @Test
    void testValidHappyPathDeliveryAndPickup() {
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        parcel.markAsInTransit("Courier", now);
        assertEquals(Parcel.ParcelState.IN_TRANSIT, parcel.getState());

        parcel.markAsReadyForPickup("Courier", now);
        assertEquals(Parcel.ParcelState.READY_FOR_PICKUP, parcel.getState());
        assertNotNull(parcel.getPickupCode(), "Pickup code should be generated");
        assertNotNull(parcel.getReadyForPickupAt(), "Ready timestamp should be set");

        parcel.markAsPickedUp("Recipient", now);
        assertEquals(Parcel.ParcelState.PICKED_UP, parcel.getState());

        assertEquals(4, parcel.getHistory().size());
        assertEquals("Recipient", parcel.getHistory().get(3).actor());
    }

    @Test
    void testValidExpirationAndReturnPath() {
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        parcel.markAsInTransit("Courier", now);
        parcel.markAsReadyForPickup("Courier", LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC));

        parcel.expire("System", now);
        assertEquals(Parcel.ParcelState.EXPIRED, parcel.getState());

        parcel.markAsReturned("Operator", now);
        assertEquals(Parcel.ParcelState.RETURNED, parcel.getState());
    }

    @Test
    void testValidCancellation() {
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        parcel.cancel("Partner", now);
        assertEquals(Parcel.ParcelState.CANCELLED, parcel.getState());
    }

    @Test
    void testValidDestruction() {
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        parcel.markAsInTransit("Courier", now);
        parcel.markAsDestroyed("Operator", now);
        assertEquals(Parcel.ParcelState.DESTROYED, parcel.getState());
    }

    @Test
    void testInvalidTransitionsFromInTransit() {
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        parcel.markAsInTransit("System", now);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            parcel.cancel("Partner", now);
        });
        assertTrue(exception.getMessage().contains("Cannot transition parcel from IN_TRANSIT to CANCELLED"));

        assertThrows(IllegalStateException.class, () -> {
            parcel.expire("System", now);
        });
    }

    @Test
    void testInvalidTransitionsFromReadyForPickup() {
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        parcel.markAsInTransit("System", now);
        parcel.markAsReadyForPickup("Courier", now);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            parcel.markAsInTransit("System", now);
        });
        assertTrue(exception.getMessage().contains("Cannot transition parcel from READY_FOR_PICKUP to IN_TRANSIT"));

        assertThrows(IllegalStateException.class, () -> {
            parcel.cancel("Partner", now);
        });
    }

    @Test
    void testInvalidTransitionsFromExpired() {
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        parcel.markAsInTransit("System", now);
        parcel.markAsReadyForPickup("Courier", now);
        parcel.expire("System", now);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            parcel.markAsPickedUp("Recipient", now);
        });
        assertTrue(exception.getMessage().contains("Cannot transition parcel from EXPIRED to PICKED_UP"));

        assertThrows(IllegalStateException.class, () -> {
            parcel.markAsDestroyed("Operator", now);
        });
    }

    @Test
    void testInvalidTransitionsThrowException() {
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            parcel.markAsPickedUp("Recipient", now);
        });

        assertTrue(exception.getMessage().contains("Cannot transition parcel from CREATED to PICKED_UP"));
    }

    @Test
    void testTerminalStatesRejectFurtherTransitions() {
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        parcel.cancel("Partner", now);

        assertThrows(IllegalStateException.class, () -> {
            parcel.markAsInTransit("Courier", now);
        });
    }

    @Test
    void testPickupCodesAreUniqueAcrossParcels() {
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        var codes = IntStream.range(0, 1000)
            .mapToObj(i -> new Parcel("S", ParcelSize.M, "u@d.com", 
            LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC)))
            .peek(p -> p.markAsInTransit("c", now))
            .peek(p -> p.markAsReadyForPickup("c", now))
            .map(Parcel::getPickupCode)
            .collect(Collectors.toSet());

        assertEquals(1000, codes.size());
    }

    @Test
    void testShouldUpdateStationWhenParcelIsRedirected() {
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        parcel.setStation("Station B");
        parcel.markAsInTransit("Courier", now);

        assertEquals("Station B", parcel.getStation());
        assertEquals(Parcel.ParcelState.IN_TRANSIT, parcel.getState());
    }

    @Test
    void testShouldUpdateContactWhenRecipientChanges() {
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        parcel.setRecipientContact("other@domain.com");
        parcel.markAsInTransit("Courier", now);
        parcel.markAsReadyForPickup("Courier", now);

        assertEquals("other@domain.com", parcel.getRecipientContact());
        assertNotNull(parcel.getPickupCode()); // pickup code was still generated
    }
}
