package ua.tqs.humberpecas.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@RequiredArgsConstructor
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

    @NonNull
    @ManyToOne
    private Person person;

    @OneToMany
    private List<Purchase> purchases;

    public Address(String address, String postalCode, String city, String country) {
        this.address = address;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
    }

    public Address() {}
}

