package ua.pt.tqs.HW1_125264.LockerHardwareSim;

public interface LockerHardwarePort {

    public enum OpenResult {
        SUCCESS,
        ALREADY_OPEN,
        HARDWARE_FAILURE
    }

    OpenResult openCompartment(Long compartmentId);
    boolean isDoorClosed(Long compartmentId);
}
