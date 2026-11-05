package ua.pt.tqs.HW1_125264.cucumber.steps;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import ua.pt.tqs.HW1_125264.Entities.Parcel;
import ua.pt.tqs.HW1_125264.Exceptions.InvalidPickupCodeException;
import ua.pt.tqs.HW1_125264.Services.ParcelService;
import ua.pt.tqs.HW1_125264.cucumber.CucumberTestSupport;
import ua.pt.tqs.HW1_125264.cucumber.ScenarioContext;

public class PickupSteps {

    @Autowired
    private ParcelService parcelService;

    @Autowired
    private CucumberTestSupport support;

    @Autowired
    private ScenarioContext context;

    @When("the recipient picks up the parcel with the correct pickup code")
    public void theRecipientPicksUpWithTheCorrectPickupCode() {
        try {
            Parcel parcel = parcelService.pickupParcel(
                context.getTrackingCode(),
                context.getPickupCode(),
                "Recipient",
                support.now());
            context.setParcel(parcel);
        } catch (Exception e) {
            context.setException(e);
        }
    }

    @When("the recipient picks up the parcel with an invalid pickup code")
    public void theRecipientPicksUpWithAnInvalidPickupCode() {
        try {
            Parcel parcel = parcelService.pickupParcel(
                context.getTrackingCode(),
                UUID.randomUUID(),
                "Recipient",
                support.now());
            context.setParcel(parcel);
        } catch (Exception e) {
            context.setException(e);
        }
    }

    @Then("the pickup is rejected as invalid")
    public void thePickupIsRejectedAsInvalid() {
        assertInstanceOf(InvalidPickupCodeException.class, context.getException());
    }
}
