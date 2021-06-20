package ua.tqs.humberpecas.model;


import lombok.Data;
import lombok.EqualsAndHashCode;
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
    @JoinColumn(name = "person_id", nullable=false)
    private Person person;

    @CreationTimestamp
    private Date date;

    @ManyToOne
    @JoinColumn(name = "address_id", nullable=false)
    @EqualsAndHashCode.Exclude
    private Address address;

    @Column(unique=true)
    private Long serviceOrderId;

    @ManyToMany
    @JoinTable(
            name = "purchase_products",
            joinColumns = @JoinColumn(name = "purchase_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"))
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
