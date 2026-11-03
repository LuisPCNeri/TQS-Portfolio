package ua.pt.tqs.HW1_125264.GeoCoding;

public interface IGeoCodingPort {
    Coordinates geocode(String address);
    record Coordinates(double latitude, double longitude) {}
}
