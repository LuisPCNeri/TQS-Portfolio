package ua.pt.tqs.HW1_125264.cucumber.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import ua.pt.tqs.HW1_125264.Entities.Parcel;
import ua.pt.tqs.HW1_125264.Exceptions.NoAvailableCompartmentException;
import ua.pt.tqs.HW1_125264.cucumber.CucumberTestSupport;
import ua.pt.tqs.HW1_125264.cucumber.ScenarioContext;

public class RegisterParcelSteps {

    @Autowired
    private CucumberTestSupport support;

    @Autowired
    private ScenarioContext context;

    @Given("the station {string} has {int} available {word} compartment(s)")
    public void theStationHasAvailableCompartments(String stationName, int count, String size) {
        assertEquals((long) count, support.availableCompartments(stationName, size));
    }

    @When("the partner registers an {string} parcel for {string} at {string}")
    public void thePartnerRegistersAParcel(String size, String recipient, String stationName) {
        try {
            Parcel parcel = support.registerParcel(size, recipient, stationName);
            context.setParcel(parcel);
            context.setTrackingCode(parcel.getTrackingCode());
            context.setCompartmentId(parcel.getCompartment().getId());
        } catch (Exception e) {
            context.setException(e);
        }
    }

    @Then("a tracking code is returned")
    public void aTrackingCodeIsReturned() {
        assertNotNull(context.getParcel());
        assertNotNull(context.getParcel().getTrackingCode());
    }

    @Then("the parcel has a pickup deadline")
    public void theParcelHasAPickupDeadline() {
        assertNotNull(context.getParcel().getPickupDeadline());
    }

    @Given("all {string} compartments at {string} are occupied")
    public void allCompartmentsAtAreOccupied(String size, String stationName) {
        support.occupyAllCompartments(stationName, size);
    }

    @Then("registration fails because no compartment is available")
    public void registrationFailsBecauseNoCompartmentIsAvailable() {
        assertInstanceOf(NoAvailableCompartmentException.class, context.getException());
    }
}
