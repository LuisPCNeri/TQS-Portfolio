package ua.pt.tqs.HW1_125264.cucumber.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import ua.pt.tqs.HW1_125264.Entities.Compartment;
import ua.pt.tqs.HW1_125264.Entities.Compartment.CompartmentStatus;
import ua.pt.tqs.HW1_125264.Entities.Parcel;
import ua.pt.tqs.HW1_125264.Entities.Parcel.ParcelState;
import ua.pt.tqs.HW1_125264.Services.CompartmentService;
import ua.pt.tqs.HW1_125264.cucumber.CucumberTestSupport;
import ua.pt.tqs.HW1_125264.cucumber.ScenarioContext;

public class OperatorSteps {

    @Autowired
    private CucumberTestSupport support;

    @Autowired
    private ScenarioContext context;

    @Autowired
    private CompartmentService compartmentService;

    @When("the operator lists {string} parcels at {string}")
    public void theOperatorListsParcelsAt(String state, String stationName) {
        context.setParcels(support.parcelsAt(stationName, ParcelState.valueOf(state)));
    }

    @Then("the list contains {int} parcel(s)")
    public void theListContainsParcels(int count) {
        assertEquals(count, context.getParcels().size());
    }

    @Then("the listed parcel is the one ready for pickup")
    public void theListedParcelIsTheOneReadyForPickup() {
        assertTrue(context.getParcels().stream().anyMatch(parcel ->
            parcel.getTrackingCode().equals(context.getReadyParcel().getTrackingCode())));
    }

    @Given("an available compartment at {string}")
    public void anAvailableCompartmentAt(String stationName) {
        Compartment compartment = support.firstAvailableCompartment(stationName);
        context.setCompartment(compartment);
        context.setCompartmentId(compartment.getId());
    }

    @When("the operator marks the compartment as out of service")
    public void theOperatorMarksTheCompartmentAsOutOfService() {
        try {
            context.setCompartment(compartmentService.markOutOfService(context.getCompartmentId()));
        } catch (Exception e) {
            context.setException(e);
        }
    }

    @Then("the compartment is {string}")
    public void theCompartmentIs(String status) {
        assertNull(context.getException(), () -> "Unexpected exception: " + context.getException());
        Compartment compartment = support.compartment(context.getCompartmentId());
        assertEquals(status, compartment.getStatus().name());
    }

    @Then("the compartment is no longer available for new parcels")
    public void theCompartmentIsNoLongerAvailableForNewParcels() {
        Compartment compartment = support.compartment(context.getCompartmentId());
        assertNotEquals(CompartmentStatus.AVAILABLE, compartment.getStatus());
    }

    @Given("an occupied compartment at {string}")
    public void anOccupiedCompartmentAt(String stationName) {
        Parcel parcel = support.registerParcel("M", "recipient@example.com", stationName);
        context.setParcel(parcel);
        context.setTrackingCode(parcel.getTrackingCode());
        context.setCompartmentId(parcel.getCompartment().getId());
    }

    @When("the operator tries to mark the compartment as out of service")
    public void theOperatorTriesToMarkTheCompartmentAsOutOfService() {
        try {
            compartmentService.markOutOfService(context.getCompartmentId());
        } catch (Exception e) {
            context.setException(e);
        }
    }

    @Then("the operation is rejected")
    public void theOperationIsRejected() {
        assertInstanceOf(IllegalStateException.class, context.getException());
    }
}
