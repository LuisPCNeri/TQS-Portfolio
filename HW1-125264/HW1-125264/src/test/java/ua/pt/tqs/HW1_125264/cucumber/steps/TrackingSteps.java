package ua.pt.tqs.HW1_125264.cucumber.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import ua.pt.tqs.HW1_125264.Entities.Parcel;
import ua.pt.tqs.HW1_125264.Services.ParcelService;
import ua.pt.tqs.HW1_125264.cucumber.ScenarioContext;

public class TrackingSteps {

    @Autowired
    private ParcelService parcelService;

    @Autowired
    private ScenarioContext context;

    @When("the parcel is tracked by its tracking code")
    public void theParcelIsTrackedByItsTrackingCode() {
        Parcel parcel = parcelService.getParcelByTrackingCode(context.getTrackingCode());
        context.setParcel(parcel);
    }

    @Then("the parcel state is {string}")
    public void theParcelStateIs(String state) {
        assertEquals(state, context.getParcel().getState().name());
    }

    @Then("the history contains the following transitions:")
    public void theHistoryContainsTheFollowingTransitions(DataTable table) {
        List<Map<String, String>> expected = table.asMaps();
        List<Parcel.StateHistory> history = context.getParcel().getHistory();

        assertEquals(expected.size(), history.size());
        for (Map<String, String> row : expected) {
            assertTrue(history.stream().anyMatch(entry ->
                entry.state().name().equals(row.get("state")) &&
                entry.actor().equals(row.get("actor"))),
                () -> "Missing history entry: " + row);
        }
    }

    @Then("every history entry has a timestamp")
    public void everyHistoryEntryHasATimestamp() {
        assertTrue(context.getParcel().getHistory().stream()
            .allMatch(entry -> entry.timestamp() != null));
    }
}
