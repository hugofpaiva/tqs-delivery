package ua.tqs.humberpecas.model;

import lombok.Data;
import javax.persistence.*;
import java.util.List;

@Data
@Entity
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String address;
    private String postalCode;
    private String city;
    private String country;

    @ManyToOne
    private Person person;

    @OneToMany
    private List<Purchase> purchase;
}
