package ua.pt.tqs.HW1_125264.Entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Parcel {

    public enum ParcelSize {
        S,
        M,
        L
    }

    public enum ParcelState {
        CREATED,
        IN_TRANSIT,
        DESTROYED,
        READY_FOR_PICKUP,
        CANCELLED,
        PICKED_UP,
        EXPIRED,
        RETURNED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embeddable
    public record StateHistory(
        @Enumerated(EnumType.STRING) ParcelState state,
        LocalDateTime timestamp,
        String actor) {}

    @ElementCollection
    private List<StateHistory> history = new ArrayList<>();

    private UUID trackingCode;
    private UUID pickupCode;

    private String station;
    private String recipientContact;

    private LocalDateTime createdAt;
    private LocalDateTime readyForPickupAt;

    @Enumerated(EnumType.STRING)
    private ParcelSize size;

    @Enumerated(EnumType.STRING)
    private ParcelState state;

    @OneToOne
    @JoinColumn(name = "compartment_id")
    private Compartment compartment;

    public Parcel(String station, ParcelSize parcelSize, String recipientContact, LocalDateTime now) {
        this.station          = station;
        this.recipientContact = recipientContact;
        this.size             = parcelSize;

        this.trackingCode = UUID.randomUUID();
        this.state        = ParcelState.CREATED;
        this.createdAt    = now;

        this.history.add(new StateHistory(ParcelState.CREATED, this.createdAt, "Partner"));
    }

    private boolean isNextStateValid(ParcelState nextState) {

        return switch (this.state) {
            case CREATED          -> nextState == ParcelState.CANCELLED || nextState == ParcelState.IN_TRANSIT;
            case IN_TRANSIT       -> nextState == ParcelState.READY_FOR_PICKUP || nextState == ParcelState.DESTROYED;
            case READY_FOR_PICKUP -> nextState == ParcelState.PICKED_UP || nextState == ParcelState.EXPIRED;
            case EXPIRED          -> nextState == ParcelState.RETURNED;
            case PICKED_UP, CANCELLED, DESTROYED, RETURNED -> false;
        };
    }

    private void transitionTo(ParcelState nextState, String actor) {
        if(!isNextStateValid(nextState)) {
            throw new IllegalStateException(
                String.format("Cannot transition parcel from %s to %s", this.state, nextState)
            );
        }

        this.state = nextState;
        this.history.add(new StateHistory(nextState, LocalDateTime.now(), actor));
    }

    public void cancel(String actor) {
        transitionTo(ParcelState.CANCELLED, actor);
    }

    public void markAsInTransit(String actor) {
        transitionTo(ParcelState.IN_TRANSIT, actor);
    }

    public void markAsDestroyed(String actor) {
        transitionTo(ParcelState.DESTROYED, actor);
    }

    public void markAsReadyForPickup(String actor, LocalDateTime now) {
        this.pickupCode       = UUID.randomUUID();
        this.readyForPickupAt = now;

        transitionTo(ParcelState.READY_FOR_PICKUP, actor);
    }

    public void markAsPickedUp(String actor) {
        transitionTo(ParcelState.PICKED_UP, actor);
    }

    public void expire(String actor) {
        transitionTo(ParcelState.EXPIRED, actor);
    }

    public void markAsReturned(String actor) {
        transitionTo(ParcelState.RETURNED, actor);
    }

    public ParcelSize getSize()   { return this.size; }
    public UUID getTrackingCode() { return this.trackingCode; }
    public UUID getPickupCode()   { return this.pickupCode; }
    public ParcelState getState() { return this.state; }
    public LocalDateTime getCreatedAt()        { return this.createdAt; }
    public LocalDateTime getReadyForPickupAt() { return this.readyForPickupAt; }

    public List<StateHistory> getHistory() { return List.copyOf(this.history); }

    public String getRecipientContact() { return this.recipientContact; }
    public void setRecipientContact(String recipientContact) { this.recipientContact = recipientContact; }

    public String getStation() { return this.station; }
    public void setStation(String station)   { this.station = station; }
}
