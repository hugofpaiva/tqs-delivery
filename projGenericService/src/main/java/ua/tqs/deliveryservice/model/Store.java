package ua.tqs.deliveryservice.model;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    private String name;
    private String description;

    @Column(unique = true)
    private String token;

    @OneToOne
    private Address address;

    public Store(String name, String description, String token, Address address) {
        this.name = name;
        this.token = token;
        this.description = description;
        this.address = address;
    }

    public Store() {}
}
