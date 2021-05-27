package ua.tqs.deliveryservice.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Date;

@Data
@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @CreationTimestamp
    private Date date;

    @ManyToOne
    private Address address;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    private Rider rider;

    @ManyToOne
    private Store store;

    @Min(value = 0, message = "Review should not be under the value of 0.")
    @Max(value = 5, message = "Review should not be above the value of 5.")
    private int riderReview;
}
