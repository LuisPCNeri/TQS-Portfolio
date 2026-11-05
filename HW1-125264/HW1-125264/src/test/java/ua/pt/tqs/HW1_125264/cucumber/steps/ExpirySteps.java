package ua.pt.tqs.HW1_125264.cucumber.steps;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import ua.pt.tqs.HW1_125264.Entities.Parcel;
import ua.pt.tqs.HW1_125264.Entities.Parcel.ParcelState;
import ua.pt.tqs.HW1_125264.Services.ParcelService;
import ua.pt.tqs.HW1_125264.cucumber.CucumberTestSupport;
import ua.pt.tqs.HW1_125264.cucumber.ScenarioContext;

public class ExpirySteps {

    @Autowired
    private CucumberTestSupport support;

    @Autowired
    private ScenarioContext context;

    @Autowired
    private ParcelService parcelService;

    @Given("the parcel's pickup deadline has passed")
    public void theParcelsPickupDeadlineHasPassed() {
        support.passPickupDeadline(context.getParcel());
    }

    @When("the expiry check runs")
    public void theExpiryCheckRuns() {
        support.runExpiryCheck();
        context.setParcel(support.reload(context.getParcel()));
    }

    @Then("the operator can see the parcel among expired parcels")
    public void theOperatorCanSeeTheParcelAmongExpiredParcels() {
        assertTrue(parcelService.getParcelsByState(ParcelState.EXPIRED).stream()
            .anyMatch(parcel -> parcel.getTrackingCode().equals(context.getTrackingCode())));
    }

    @When("the operator registers the parcel for removal")
    public void theOperatorRegistersTheParcelForRemoval() {
        Parcel parcel = parcelService.markAsReturned(context.getTrackingCode(), support.now());
        context.setParcel(parcel);
    }
}
