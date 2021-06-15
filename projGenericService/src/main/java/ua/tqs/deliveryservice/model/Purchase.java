package ua.tqs.deliveryservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

@Data
@Entity
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    private Date date;

    @OneToOne
    private Address address;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JsonIgnore
    private Rider rider;

    @ManyToOne
    private Store store;

    private String clientName;
    private Long deliveryTime;

    @Min(value = 0, message = "Review should not be under the value of 0.")
    @Max(value = 5, message = "Review should not be above the value of 5.")
    private Integer riderReview;

    public Purchase(Address address, Store store, String clientName) {
        this.address = address;
        this.store = store;
        this.status = Status.PENDENT;
        this.clientName = clientName;
        this.date = new Date(); // just for testing
    }

    public Purchase(Address address, Date date, Store store, String clientName) {
        this.address = address;
        this.store = store;
        this.status = Status.PENDENT;
        this.clientName = clientName;
        this.date = date;
    }

    public Purchase(Address address, Rider rider, Store store, String clientName) {
        this.address = address;
        this.rider = rider;
        this.store = store;
        this.status = Status.ACCEPTED;
        this.clientName = clientName;
    }

    public Purchase() {}

    public Map<String, Object> getMap() {
        Map<String, Object> map = new TreeMap<>();
        map.put("orderId", id);
        map.put("date", date);
        map.put("store", store.getMap());
        map.put("clientName", clientName);
        map.put("status", status);
        map.put("clientAddress", address.getMap());
        return map;
    }
}
