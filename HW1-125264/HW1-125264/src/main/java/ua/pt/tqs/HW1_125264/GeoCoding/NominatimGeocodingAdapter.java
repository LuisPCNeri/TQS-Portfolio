package ua.pt.tqs.HW1_125264.GeoCoding;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.ObjectMapper;
import ua.pt.tqs.HW1_125264.Exceptions.GeocodingException;

@Component
@Profile("!prod")
public class NominatimGeocodingAdapter implements IGeoCodingPort {

    private final RestClient restClient;

    public NominatimGeocodingAdapter(
        @Value("${nominatim.base-url:https://nominatim.openstreetmap.org}") String baseUrl) {
        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("User-Agent", "TuttiBox/1.0")
            .build();
    }

    @Override
    public Coordinates geocode(String address) {
        try {
            String response = restClient.get()
                .uri("/search?q={address}&format=json&limit=1", address)
                .retrieve()
                .body(String.class);

            var mapper = new ObjectMapper();
            var node   = mapper.readTree(response);
            if (node.isEmpty()) throw new GeocodingException("Address not found: " + address);
            double lat = node.get(0).get("lat").asDouble();
            double lon = node.get(0).get("lon").asDouble();
            return new Coordinates(lat, lon);
        } catch (GeocodingException e) {
            throw e;
        } catch (Exception e) {
            throw new GeocodingException("Geocoding failed: " + e.getMessage());
        }
    }

}
