package ua.tqs.deliveryservice.model;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;
    private String description;

    @OneToOne
    private Address address;
}
