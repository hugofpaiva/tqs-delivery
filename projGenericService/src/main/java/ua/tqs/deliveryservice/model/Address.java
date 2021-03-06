package ua.tqs.deliveryservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;


import javax.persistence.*;
import java.util.Map;
import java.util.TreeMap;

@Data
@Entity
public class Address {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    private String address;
    private String postalCode;
    private String city;
    private String country;

    @OneToOne(mappedBy = "address")
    @JsonIgnore
    private Store store;

    @OneToOne(mappedBy = "address")
    @JsonIgnore
    private Purchase purchase;

    public Address(String address, String postalCode, String city, String country) {
        this.address = address;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
    }

    public Address() {}

    @JsonIgnore
    public Map<String, Object> getMap() {
        Map<String, Object> map = new TreeMap<>();
        map.put("address", address);
        map.put("postalCode", postalCode);
        map.put("city", city);
        map.put("country", country);
        return map;
    }

}
