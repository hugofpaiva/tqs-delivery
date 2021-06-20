package ua.tqs.humberpecas.model;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NonNull
    private String address;

    @NonNull
    private String postalCode;

    @NonNull
    private String city;

    @NonNull
    private String country;

    @ManyToOne
    @JoinColumn(name = "person_id", nullable=false)
    @EqualsAndHashCode.Exclude
    private Person person;


    @ToString.Exclude
    @OneToMany(mappedBy = "address")
    private List<Purchase> purchases;

    public Address(String address, String postalCode, String city, String country, Person person) {
        this.address = address;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
        this.person = person;
        this.purchases = new ArrayList<>();
    }

    public Address(String address, String postalCode, String city, String country) {
        this.address = address;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
        this.purchases = new ArrayList<>();
    }


}

