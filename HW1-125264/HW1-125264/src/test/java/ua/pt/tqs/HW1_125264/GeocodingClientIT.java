package ua.pt.tqs.HW1_125264;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import ua.pt.tqs.HW1_125264.Exceptions.GeocodingException;
import ua.pt.tqs.HW1_125264.GeoCoding.IGeoCodingPort;


@SpringBootTest
@Testcontainers
@WireMockTest(httpPort = 8089)
@ActiveProfiles("test")
class GeocodingClientIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18");

    @Autowired IGeoCodingPort geocodingPort;

    @Test
    void testGeocodesAddressSuccessfully() {
        stubFor(get(urlPathEqualTo("/search"))
            .willReturn(okJson("""
                [{"lat": "41.1496", "lon": "-8.6109", "display_name": "Porto"}]
                """)));

        var coords = geocodingPort.geocode("Porto Central");

        assertEquals(41.1496, coords.latitude(),  0.0001);
        assertEquals(-8.6109, coords.longitude(), 0.0001);
    }

    @Test
    void testGeocodingProviderDownThrowsException() {
        stubFor(get(urlPathEqualTo("/search"))
            .willReturn(serverError()));

        assertThrows(GeocodingException.class, () ->
            geocodingPort.geocode("Porto Central"));
    }

    @Test
    void testEmptyResponseThrowsException() {
        stubFor(get(urlPathEqualTo("/search"))
            .willReturn(okJson("[]")));

        assertThrows(GeocodingException.class, () ->
            geocodingPort.geocode("Unknown Place XYZ"));
    }
}
