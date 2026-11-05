package ua.pt.tqs.HW1_125264.cucumber;

import java.util.HashMap;
import java.util.Map;

import ua.pt.tqs.HW1_125264.GeoCoding.IGeoCodingPort;

public class StubGeocodingPort implements IGeoCodingPort {

    private final Map<String, Coordinates> coordinates = new HashMap<>();

    public void stub(String address, double latitude, double longitude) {
        coordinates.put(address, new Coordinates(latitude, longitude));
    }

    @Override
    public Coordinates geocode(String address) {
        Coordinates result = coordinates.get(address);
        if (result == null) {
            throw new IllegalArgumentException("No geocoding stub for address: " + address);
        }
        return result;
    }

    public void reset() {
        coordinates.clear();
    }
}
