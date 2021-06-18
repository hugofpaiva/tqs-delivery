package ua.tqs.deliveryservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import javax.persistence.*;
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

    public Store(String name, String description, String token, Address address) {
        this.name = name;
        this.token = token;
        this.description = description;
        this.address = address;
    }

    public Store() {}

    @JsonIgnore
    public Map<String, Object> getMap() {
        Map<String, Object> map = new TreeMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("description", description);
        map.put("address", address.getMap());
        return map;
    }
}
