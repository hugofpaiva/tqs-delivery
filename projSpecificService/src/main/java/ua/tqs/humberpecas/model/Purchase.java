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
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @ManyToOne
    private Person person;

    @CreationTimestamp
    private Date date;

    @ManyToOne
    private Address address;

    @Column(unique=true)
    private Long serviceOrderId;

    @ManyToMany
    private List<Product> products;


    private String riderName;
    private int review;


    @Enumerated(value = EnumType.STRING)
    private PurchaseStatus status;

    public Purchase(Person person, Address address, List<Product> products) {
        this.person = person;
        this.address = address;
        this.products = products;
        this.status = PurchaseStatus.PENDENT;
    }

    public Purchase(){ }

}
