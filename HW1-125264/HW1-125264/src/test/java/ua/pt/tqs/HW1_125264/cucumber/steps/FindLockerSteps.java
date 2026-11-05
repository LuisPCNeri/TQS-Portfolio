package ua.pt.tqs.HW1_125264.cucumber.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import ua.pt.tqs.HW1_125264.Controllers.StationController;
import ua.pt.tqs.HW1_125264.DTOs.LockerStationDTO;
import ua.pt.tqs.HW1_125264.cucumber.ScenarioContext;
import ua.pt.tqs.HW1_125264.cucumber.StubGeocodingPort;

public class FindLockerSteps {

    @Autowired
    private StationController stationController;

    @Autowired
    private StubGeocodingPort geocodingPort;

    @Autowired
    private ScenarioContext context;

    @Given("the geocoding service locates {string} at {double}, {double}")
    public void theGeocodingServiceLocates(String address, double latitude, double longitude) {
        geocodingPort.stub(address, latitude, longitude);
    }

    @When("the recipient searches for lockers near {string}")
    public void theRecipientSearchesForLockersNear(String address) {
        context.setStations(stationController.findNearestStations(address));
    }

    @Then("the nearest station is {string}")
    public void theNearestStationIs(String name) {
        assertFalse(context.getStations().isEmpty());
        assertEquals(name, context.getStations().get(0).getName());
    }

    @Then("the station offers {int} free {word} compartment(s)")
    public void theStationOffersFreeCompartments(int count, String size) {
        LockerStationDTO nearest = context.getStations().get(0);
        assertEquals((long) count, nearest.getFreeCompartmentsBySize().get(size));
    }
}
