package ua.tqs.humberpecas.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@Entity
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    private Person person;

    @CreationTimestamp
    private Date date;

    @ManyToOne
    private Address address;

    private Long service_order_id;

    @ManyToMany
    private List<Product> products;

    public Purchase(Person person, Address address, List<Product> products) {
        this.person = person;
        this.address = address;
        this.products = products;
    }

    public Purchase() {}
}
