package ua.pt.tqs.HW1_125264.LockerHardwareSim;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class InMemoryLockerHardwareFake implements LockerHardwarePort {

    private final Set<Long> openDoors       = new HashSet<>();
    private final Set<Long> faultyCompartments = new HashSet<>();
    private boolean globalFailure = false;

    @Override
    public OpenResult openCompartment(Long compartmentId) {
        if (globalFailure || faultyCompartments.contains(compartmentId)) {
            return OpenResult.HARDWARE_FAILURE;
        }
        if (openDoors.contains(compartmentId)) {
            return OpenResult.ALREADY_OPEN;
        }
        openDoors.add(compartmentId);
        return OpenResult.SUCCESS;
    }

    @Override
    public boolean isDoorClosed(Long compartmentId) {
        if (faultyCompartments.contains(compartmentId)) {
            return false;
        }
        return !openDoors.contains(compartmentId);
    }

    public void closeDoor(Long compartmentId) {
        openDoors.remove(compartmentId);
    }

    public void setFaulty(Long compartmentId) {
        faultyCompartments.add(compartmentId);
    }

    public void clearFaulty(Long compartmentId) {
        faultyCompartments.remove(compartmentId);
    }

    public void setGlobalFailure(boolean fail) {
        this.globalFailure = fail;
    }

    public void reset() {
        openDoors.clear();
        faultyCompartments.clear();
        globalFailure = false;
    }
}
