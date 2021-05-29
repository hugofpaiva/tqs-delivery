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

    private long service_order_id;

    @ManyToMany
    private List<Product> products;

    @Enumerated(value = EnumType.STRING)
    private PurchageStatus status;

    public Purchase(Address address, List<Product> products) {
        this.address = address;
        this.products = products;
        this.status = PurchageStatus.PENDENT;
    }

}
