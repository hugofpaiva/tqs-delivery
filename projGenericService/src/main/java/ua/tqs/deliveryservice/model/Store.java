package ua.tqs.deliveryservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Data
@Entity
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    private String name;
    private String description;

    @Column(unique = true)
    @JsonIgnore
    private String token;

    @OneToOne
    private Address address;

    @JsonIgnore
    @OneToMany(mappedBy = "store")
    private Set<Purchase> purchases;

    @Column(unique = true)
    private String storeUrl;

    private Double latitude;
    private Double longitude;

    public Store(String name, String description, String token, Address address, String storeUrl) {

        this.name = name;
        this.token = token;
        this.description = description;
        this.address = address;
        this.storeUrl = storeUrl;
        this.longitude = 0.0;
        this.latitude = 0.0;
    }

    public Store(String name, String description, String token, Address address, String storeUrl,  double latitude, double longitude) {
        this.name = name;
        this.token = token;
        this.description = description;
        this.address = address;
        this.longitude = longitude;
        this.latitude = latitude;
        this.storeUrl = storeUrl;
    }

    public Store() {}

    @JsonIgnore
    public Map<String, Object> getMap() {
        Map<String, Object> map = new TreeMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("description", description);
        map.put("address", address.getMap());
        map.put("latitude", latitude);
        map.put("longitude", longitude);
        return map;
    }
}
