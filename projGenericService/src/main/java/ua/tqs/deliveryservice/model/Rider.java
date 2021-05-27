package ua.tqs.deliveryservice.model;

import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

@Data
@Entity
public class Rider extends User {

    private long reviewsSum;
    private int totalNumReviews;

    @OneToMany
    private List<Order> orders;
}
