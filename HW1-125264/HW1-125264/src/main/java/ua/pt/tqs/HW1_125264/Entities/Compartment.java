package ua.pt.tqs.HW1_125264.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.pt.tqs.HW1_125264.Entities.Parcel.ParcelSize;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Compartment {

    public enum CompartmentStatus {
        AVAILABLE,
        OCCUPIED,
        FAULTY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ParcelSize size;

    @Enumerated(EnumType.STRING)
    private CompartmentStatus status;

    @ManyToOne
    @JoinColumn(name = "station_id")
    private LockerStation station;

    @OneToOne(mappedBy = "compartment")
    private Parcel parcel;

}
