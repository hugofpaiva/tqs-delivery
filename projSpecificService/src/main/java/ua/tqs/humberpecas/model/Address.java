package ua.tqs.humberpecas.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @ManyToOne
    @JsonIgnore
    private Person person;

    @OneToMany
    @JsonIgnore
    private List<Purchase> purchases;

    public Address(String address, String postalCode, String city, String country, Person person) {
        this.address = address;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
        this.person = person;
    }

}

