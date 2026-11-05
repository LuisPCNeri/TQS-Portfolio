package ua.pt.tqs.HW1_125264.cucumber.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import ua.pt.tqs.HW1_125264.Entities.Compartment;
import ua.pt.tqs.HW1_125264.Entities.Parcel;
import ua.pt.tqs.HW1_125264.Exceptions.HardwareFailureException;
import ua.pt.tqs.HW1_125264.LockerHardwareSim.InMemoryLockerHardwareFake;
import ua.pt.tqs.HW1_125264.cucumber.CucumberTestSupport;
import ua.pt.tqs.HW1_125264.cucumber.ScenarioContext;

public class CommonSteps {

    @Autowired
    private CucumberTestSupport support;

    @Autowired
    private ScenarioContext context;

    @Autowired
    private InMemoryLockerHardwareFake hardwareFake;

    @Given("a registered {string} parcel for {string} at {string}")
    public void aRegisteredParcel(String size, String recipient, String stationName) {
        Parcel parcel = support.registerParcel(size, recipient, stationName);
        context.setParcel(parcel);
        context.setTrackingCode(parcel.getTrackingCode());
        context.setCompartmentId(parcel.getCompartment().getId());
    }

    @Given("a parcel ready for pickup at {string}")
    public void aParcelReadyForPickup(String stationName) {
        Parcel parcel = support.readyForPickup(stationName);
        context.setParcel(parcel);
        context.setReadyParcel(parcel);
        context.setTrackingCode(parcel.getTrackingCode());
        context.setPickupCode(parcel.getPickupCode());
        context.setCompartmentId(parcel.getCompartment().getId());
    }

    @When("the courier drops off the parcel")
    public void theCourierDropsOffTheParcel() {
        try {
            Parcel parcel = support.dropOff(context.getParcel());
            context.setParcel(parcel);
            context.setTrackingCode(parcel.getTrackingCode());
        } catch (Exception e) {
            context.setException(e);
        }
    }

    @Then("the parcel is in state {string}")
    public void theParcelIsInState(String state) {
        assertNull(context.getException(), () -> "Unexpected exception: " + context.getException());
        assertNotNull(context.getParcel());
        assertEquals(state, context.getParcel().getState().name());
    }

    @Then("the parcel remains in state {string}")
    public void theParcelRemainsInState(String state) {
        Parcel reloaded = support.reload(context.getParcel());
        assertEquals(state, reloaded.getState().name());
    }

    @Then("a pickup code is issued")
    public void aPickupCodeIsIssued() {
        assertNotNull(context.getParcel().getPickupCode());
        context.setPickupCode(context.getParcel().getPickupCode());
    }

    @Then("the reserved compartment was opened")
    public void theReservedCompartmentWasOpened() {
        assertFalse(hardwareFake.isDoorClosed(context.getCompartmentId()));
    }

    @Then("the compartment is available again")
    public void theCompartmentIsAvailableAgain() {
        Compartment compartment = support.compartment(context.getCompartmentId());
        assertEquals(Compartment.CompartmentStatus.AVAILABLE, compartment.getStatus());
    }

    @Then("the compartment is still occupied")
    public void theCompartmentIsStillOccupied() {
        Compartment compartment = support.compartment(context.getCompartmentId());
        assertEquals(Compartment.CompartmentStatus.OCCUPIED, compartment.getStatus());
    }

    @Given("the compartment hardware has failed")
    public void theCompartmentHardwareHasFailed() {
        hardwareFake.setFaulty(context.getCompartmentId());
    }

    @Then("a hardware failure is reported")
    public void aHardwareFailureIsReported() {
        assertInstanceOf(HardwareFailureException.class, context.getException());
    }
}
