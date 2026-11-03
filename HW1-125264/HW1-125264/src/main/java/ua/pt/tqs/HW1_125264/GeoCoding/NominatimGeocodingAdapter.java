package ua.pt.tqs.HW1_125264.GeoCoding;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.ObjectMapper;
import ua.pt.tqs.HW1_125264.Exceptions.GeocodingException;

@Component
@Profile("!test")
public class NominatimGeocodingAdapter implements IGeoCodingPort {

    private final RestClient restClient;

    public NominatimGeocodingAdapter() {
        this.restClient = RestClient.builder()
            .baseUrl("https://nominatim.openstreetmap.org")
            .defaultHeader("User-Agent", "TuttiBox/1.0")
            .build();
    }

    @Override
    public Coordinates geocode(String address) {
        String response = restClient.get()
            .uri("/search?q={address}&format=json&limit=1", address)
            .retrieve()
            .body(String.class);

        try {
            var mapper = new ObjectMapper();
            var node   = mapper.readTree(response);
            if (node.isEmpty()) throw new GeocodingException("Address not found: " + address);
            double lat = node.get(0).get("lat").asDouble();
            double lon = node.get(0).get("lon").asDouble();
            return new Coordinates(lat, lon);
        } catch (Exception e) {
            throw new GeocodingException("Geocoding failed: " + e.getMessage());
        }
    }

}
