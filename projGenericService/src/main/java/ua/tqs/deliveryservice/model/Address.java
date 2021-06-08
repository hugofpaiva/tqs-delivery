package ua.tqs.deliveryservice.model;

import lombok.Data;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

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

    @OneToOne(mappedBy = "address")
    private Store store;

    @OneToOne(mappedBy = "address")
    private Purchase purchase;

    public Address(String address, String postalCode, String city, String country) {
        this.address = address;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
    }

    public Address() {}

    public Map<String, Object> getMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("address", address);
        map.put("postalCode", postalCode);
        map.put("city", city);
        map.put("country", country);
        return map;
    }

}
