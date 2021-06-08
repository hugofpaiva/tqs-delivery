package ua.tqs.deliveryservice.model;

import lombok.Data;
import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Data
@Entity
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    public Map<String, Object> getMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("address", address.getMap());
        return map;
    }
}
