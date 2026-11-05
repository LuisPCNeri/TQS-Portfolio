package ua.pt.tqs.HW1_125264.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.RequestOptions;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfSystemProperty(named = "browser.tests", matches = "true")
class BrowserUseCaseIT {

    private static final String FRONTEND_URL =
        System.getProperty("e2e.frontend.url", "http://localhost:3000");
    private static final String BACKEND_URL =
        System.getProperty("e2e.backend.url", "http://localhost:8080");
    private static final String DB_URL =
        System.getProperty("e2e.db.url", "jdbc:postgresql://localhost:5432/hw1_125264");
    private static final String DB_USER = System.getProperty("e2e.db.user", "postgres");
    private static final String DB_PASSWORD = System.getProperty("e2e.db.password", "postgres");
    private static final ObjectMapper JSON = new ObjectMapper();

    private Playwright playwright;
    private APIRequestContext api;
    private Browser browser;
    private BrowserContext context;
    private Page page;
    private Long testStationId;
    private String testStationName;

    @BeforeAll
    void startBrowserAndWaitForStack() throws InterruptedException {
        playwright = Playwright.create();
        api = playwright.request().newContext(
            new APIRequest.NewContextOptions().setBaseURL(BACKEND_URL));
        waitFor(BACKEND_URL + "/actuator/health");
        waitFor(FRONTEND_URL);
        browser = playwright.chromium().launch(
            new BrowserType.LaunchOptions().setHeadless(true));
    }

    @AfterAll
    void stopBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (api != null) {
            api.dispose();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        createTestStation();
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void tearDown() throws SQLException {
        try {
            cleanupTestStation();
        } finally {
            if (context != null) {
                context.close();
            }
        }
    }

    @Test
    void uc1_findNearestLocker() {
        page.route("**/api/v1/stations**", route -> route.fulfill(
            new Route.FulfillOptions()
                .setStatus(200)
                .setContentType("application/json")
                .setBody("""
                    [{"id":1,"name":"Porto Central","addr":"Rua de Santa Catarina 100, Porto",
                      "latitude":41.1496,"longitude":-8.6109,
                      "freeCompartmentsBySize":{"S":2,"M":2,"L":1}}]
                    """)));

        page.navigate(FRONTEND_URL + "/find");
        page.getByPlaceholder("Enter your address").fill("Rua de Santa Catarina, Porto");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search")).click();

        Locator card = page.getByText("Porto Central").locator("..");
        assertThat(card).isVisible();
        assertThat(card.getByText("S: 2 free")).isVisible();
        assertThat(card.getByText("M: 2 free")).isVisible();
        assertThat(card.getByText("L: 1 free")).isVisible();
    }

    @Test
    void uc3_dropOffParcel() {
        String trackingCode = createParcel("M");

        page.navigate(FRONTEND_URL + "/terminal");
        Locator dropOff = page.locator("form").first();
        dropOff.locator("input[name='trackingCode']").fill(trackingCode);
        dropOff.locator("input[name='actor']").fill("Courier");
        dropOff.getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName("Drop Off")).click();

        assertThat(page.getByTestId("result-message")).isVisible();
        assertEquals("READY_FOR_PICKUP", parcelState(trackingCode));
    }

    @Test
    void uc4_pickUpParcel() {
        String trackingCode = createParcel("M");
        String pickupCode = dropOff(trackingCode);

        page.navigate(FRONTEND_URL + "/terminal");
        Locator pickUp = page.locator("form").nth(1);
        pickUp.locator("input[name='trackingCode']").fill(trackingCode);
        pickUp.locator("input[name='pickupCode']").fill(pickupCode);
        pickUp.getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName("Pick Up")).click();

        assertThat(page.getByTestId("result-message")).isVisible();
        assertEquals("PICKED_UP", parcelState(trackingCode));

        JsonNode history = getParcel(trackingCode).get("history");
        assertEquals("Recipient", history.get(history.size() - 1).get("actor").asText());
    }

    @Test
    void uc5_trackParcel() {
        String trackingCode = createParcel("M");
        dropOff(trackingCode);

        page.navigate(FRONTEND_URL + "/track");
        page.locator("input[name='trackingCode']").fill(trackingCode);
        page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Track")).click();

        assertThat(page.getByText("READY_FOR_PICKUP").first()).isVisible();
        assertThat(page.getByText(testStationName)).isVisible();
        assertThat(page.getByText("CREATED")).isVisible();
        assertThat(page.getByText("IN_TRANSIT")).isVisible();
        assertThat(page.getByText("Courier").first()).isVisible();
    }

    @Test
    void uc6_operatorListsParcelsAndManagesCompartments() {
        String created = createParcel("S");
        String ready = createParcel("M");
        dropOff(ready);
        long compartmentId = firstAvailableCompartment(testStationId);

        page.navigate(FRONTEND_URL + "/operator");
        page.getByLabel("Station").selectOption(testStationName);

        assertThat(parcelRow(created)).isVisible();
        assertThat(parcelRow(ready)).isVisible();

        page.getByLabel("Parcel state").selectOption("READY_FOR_PICKUP");
        assertThat(parcelRow(ready)).isVisible();
        assertThat(parcelRow(created)).hasCount(0);

        Locator compartmentRow = compartmentRow(compartmentId);
        compartmentRow.getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName("Put out of service")).click();

        assertThat(page.getByText(
            "Compartment " + compartmentId + " put out of service.")).isVisible();
        assertThat(compartmentRow.getByText("FAULTY")).isVisible();
    }

    @Test
    void uc6_operatorReturnsExpiredParcel() throws SQLException {
        String trackingCode = createParcel("M");
        dropOff(trackingCode);
        markExpired(trackingCode);

        page.navigate(FRONTEND_URL + "/operator");
        page.getByLabel("Station").selectOption(testStationName);
        page.getByLabel("Parcel state").selectOption("EXPIRED");

        Locator row = parcelRow(trackingCode);
        assertThat(row).isVisible();
        row.getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName("Register removal")).click();

        assertThat(page.getByText(
            "Parcel " + trackingCode + " registered as removed.")).isVisible();
        assertThat(parcelRow(trackingCode)).hasCount(0);
        assertEquals("RETURNED", parcelState(trackingCode));
    }

    private void waitFor(String url) throws InterruptedException {
        PlaywrightException last = null;
        for (int attempt = 0; attempt < 60; attempt++) {
            try {
                APIResponse response = api.get(url);
                boolean ok = response.ok();
                response.dispose();
                if (ok) {
                    return;
                }
            } catch (PlaywrightException e) {
                last = e;
            }
            Thread.sleep(500);
        }
        throw new IllegalStateException(
            "Stack not reachable at " + url + ". Start it with: docker compose up -d --build",
            last);
    }

    private void createTestStation() throws SQLException {
        testStationId = null;
        testStationName = "E2E Station " + UUID.randomUUID();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            try (PreparedStatement station = connection.prepareStatement(
                    "INSERT INTO locker_station (name, addr, latitude, longitude) "
                        + "VALUES (?, ?, ?, ?) RETURNING id")) {
                station.setString(1, testStationName);
                station.setString(2, "E2E Address " + UUID.randomUUID());
                station.setDouble(3, 0.0);
                station.setDouble(4, 0.0);
                try (ResultSet result = station.executeQuery()) {
                    result.next();
                    testStationId = result.getLong("id");
                }
            }

            try (PreparedStatement compartment = connection.prepareStatement(
                    "INSERT INTO compartment (size, status, station_id) "
                        + "VALUES (?, 'AVAILABLE', ?)")) {
                for (String size : List.of("S", "S", "M", "M", "L", "L")) {
                    compartment.setString(1, size);
                    compartment.setLong(2, testStationId);
                    compartment.addBatch();
                }
                compartment.executeBatch();
            }
        }
    }

    private void cleanupTestStation() throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            if (testStationName != null) {
                try (PreparedStatement history = connection.prepareStatement(
                        "DELETE FROM parcel_history WHERE parcel_id IN "
                            + "(SELECT id FROM parcel WHERE station = ?)")) {
                    history.setString(1, testStationName);
                    history.executeUpdate();
                }
                try (PreparedStatement parcels = connection.prepareStatement(
                        "DELETE FROM parcel WHERE station = ?")) {
                    parcels.setString(1, testStationName);
                    parcels.executeUpdate();
                }
            }

            if (testStationName != null) {
                try (PreparedStatement compartments = connection.prepareStatement(
                        "DELETE FROM compartment WHERE station_id IN "
                            + "(SELECT id FROM locker_station WHERE name = ?)")) {
                    compartments.setString(1, testStationName);
                    compartments.executeUpdate();
                }
                try (PreparedStatement station = connection.prepareStatement(
                        "DELETE FROM locker_station WHERE name = ?")) {
                    station.setString(1, testStationName);
                    station.executeUpdate();
                }
            }
        } finally {
            testStationId = null;
            testStationName = null;
        }
    }

    private String createParcel(String size) {
        String body = "{\"size\":\"" + size
            + "\",\"recipientContact\":\"recipient@example.com\"}";
        APIResponse response = api.post("/api/v1/parcels?stationId=" + testStationId,
            RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(body));
        assertTrue(response.ok(), () -> "Create parcel failed: " + response.text());
        return JSON.readTree(response.text()).get("trackingCode").asText();
    }

    private String dropOff(String trackingCode) {
        APIResponse response = api.put(
            "/api/v1/parcels/" + trackingCode + "/dropoff?actor=Courier");
        assertTrue(response.ok(), () -> "Drop-off failed: " + response.text());
        return JSON.readTree(response.text()).get("pickupCode").asText();
    }

    private String parcelState(String trackingCode) {
        return getParcel(trackingCode).get("state").asText();
    }

    private JsonNode getParcel(String trackingCode) {
        APIResponse response = api.get("/api/v1/parcels/" + trackingCode);
        assertTrue(response.ok(), () -> "Get parcel failed: " + response.text());
        return JSON.readTree(response.text());
    }

    private long firstAvailableCompartment(long stationId) {
        JsonNode compartments = JSON.readTree(
            api.get("/api/v1/operator/stations/" + stationId + "/compartments").text());
        for (JsonNode compartment : compartments) {
            if ("AVAILABLE".equals(compartment.get("status").asText())) {
                return compartment.get("id").asLong();
            }
        }
        throw new IllegalStateException("No available compartment at station " + stationId);
    }

    private void markExpired(String trackingCode) throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(
                 "UPDATE parcel SET state = 'EXPIRED', pickup_deadline = ? "
                     + "WHERE tracking_code = ?")) {
            statement.setObject(1, LocalDateTime.now().minusHours(1));
            statement.setObject(2, UUID.fromString(trackingCode));
            assertEquals(1, statement.executeUpdate());
        }
    }

    private Locator parcelRow(String trackingCode) {
        return page.locator("table").first().locator("tbody tr")
            .filter(new Locator.FilterOptions().setHas(
                page.getByText(trackingCode, new Page.GetByTextOptions().setExact(true))));
    }

    private Locator compartmentRow(long compartmentId) {
        return page.locator("table").nth(1).locator("tbody tr")
            .filter(new Locator.FilterOptions().setHas(
                page.getByText(String.valueOf(compartmentId),
                    new Page.GetByTextOptions().setExact(true))));
    }
}
