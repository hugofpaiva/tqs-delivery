package ua.tqs.deliveryservice.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.springframework.data.repository.cdi.Eager;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Date;

@Data
@Entity
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @CreationTimestamp
    private Date date;

    @OneToOne
    private Address address;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    private Rider rider;

    @ManyToOne
    private Store store;

    private String clientName;

    @Min(value = 0, message = "Review should not be under the value of 0.")
    @Max(value = 5, message = "Review should not be above the value of 5.")
    private Integer riderReview;

    public Purchase(Address address, Rider rider, Store store, String clientName) {
        this.address = address;
        this.rider = rider;
        this.store = store;
        this.status = Status.PENDENT;
        this.clientName = clientName;
    }

    public Purchase() {}
}
