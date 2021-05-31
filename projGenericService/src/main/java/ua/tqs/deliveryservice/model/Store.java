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

    private String pwd;

    @OneToOne
    private Address address;

    public Store(String name, String description, String pwd, Address address) {
        this.name = name;
        this.description = description;
        this.pwd = pwd;
        this.address = address;
    }

    public Store() {}
}
