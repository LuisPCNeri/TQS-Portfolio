package ua.pt.tqs.HW1_125264;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ua.pt.tqs.HW1_125264.LockerHardwareSim.InMemoryLockerHardwareFake;
import ua.pt.tqs.HW1_125264.LockerHardwareSim.LockerHardwarePort.OpenResult;

class InMemoryLockerHardwareFakeTest {

    private InMemoryLockerHardwareFake hardware;

    @BeforeEach
    void setup() {
        hardware = new InMemoryLockerHardwareFake();
    }

    @Test
    void testOpenAvailableCompartmentSuccessfully() {
        assertEquals(OpenResult.SUCCESS, hardware.openCompartment(1L));
    }

    @Test
    void testDoorIsOpenAfterOpening() {
        hardware.openCompartment(1L);
        assertFalse(hardware.isDoorClosed(1L));
    }

    @Test
    void testDoorIsClosedInitially() {
        assertTrue(hardware.isDoorClosed(1L));
    }

    @Test
    void testAlreadyOpenReturnedIfCompartmentIsOpen() {
        hardware.openCompartment(1L);
        assertEquals(OpenResult.ALREADY_OPEN, hardware.openCompartment(1L));
    }

    @Test
    void testHardwareFailureForFaultyCompartment() {
        hardware.setFaulty(1L);
        assertEquals(OpenResult.HARDWARE_FAILURE, hardware.openCompartment(1L));
    }

    @Test
    void testDoorReportedOpenWhenCompartmentIsFaulty() {
        hardware.setFaulty(1L);
        assertFalse(hardware.isDoorClosed(1L));
    }

    @Test
    void testHardwareFailureOnGlobalFailure() {
        hardware.setGlobalFailure(true);
        assertEquals(OpenResult.HARDWARE_FAILURE, hardware.openCompartment(2L));
    }

    @Test
    void testDoorClosedAfterCloseCall() {
        hardware.openCompartment(1L);
        hardware.closeDoor(1L);
        assertTrue(hardware.isDoorClosed(1L));
    }

    @Test
    void testOpenAllowedAfterClearingFault() {
        hardware.setFaulty(1L);
        hardware.clearFaulty(1L);
        assertEquals(OpenResult.SUCCESS, hardware.openCompartment(1L));
    }

    @Test
    void testResetClearsAllState() {
        hardware.openCompartment(1L);
        hardware.setFaulty(2L);
        hardware.setGlobalFailure(true);
        hardware.reset();

        assertEquals(OpenResult.SUCCESS, hardware.openCompartment(1L));
        assertEquals(OpenResult.SUCCESS, hardware.openCompartment(2L));
    }
}
