package ua.pt.tqs.HW1_125264.cucumber;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import io.cucumber.spring.ScenarioScope;
import ua.pt.tqs.HW1_125264.DTOs.LockerStationDTO;
import ua.pt.tqs.HW1_125264.Entities.Compartment;
import ua.pt.tqs.HW1_125264.Entities.Parcel;

@Component
@ScenarioScope
public class ScenarioContext {

    private Parcel parcel;
    private Parcel readyParcel;
    private UUID trackingCode;
    private UUID pickupCode;
    private Long compartmentId;
    private List<LockerStationDTO> stations;
    private List<Parcel> parcels;
    private Compartment compartment;
    private Exception exception;

    public Parcel getParcel() {
        return parcel;
    }

    public void setParcel(Parcel parcel) {
        this.parcel = parcel;
    }

    public Parcel getReadyParcel() {
        return readyParcel;
    }

    public void setReadyParcel(Parcel readyParcel) {
        this.readyParcel = readyParcel;
    }

    public UUID getTrackingCode() {
        return trackingCode;
    }

    public void setTrackingCode(UUID trackingCode) {
        this.trackingCode = trackingCode;
    }

    public UUID getPickupCode() {
        return pickupCode;
    }

    public void setPickupCode(UUID pickupCode) {
        this.pickupCode = pickupCode;
    }

    public Long getCompartmentId() {
        return compartmentId;
    }

    public void setCompartmentId(Long compartmentId) {
        this.compartmentId = compartmentId;
    }

    public List<LockerStationDTO> getStations() {
        return stations;
    }

    public void setStations(List<LockerStationDTO> stations) {
        this.stations = stations;
    }

    public List<Parcel> getParcels() {
        return parcels;
    }

    public void setParcels(List<Parcel> parcels) {
        this.parcels = parcels;
    }

    public Compartment getCompartment() {
        return compartment;
    }

    public void setCompartment(Compartment compartment) {
        this.compartment = compartment;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
