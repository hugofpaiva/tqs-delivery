package ua.tqs.humberpecas.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Date;
import java.util.List;

@Data
@Entity
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @ManyToOne
    @JsonIgnore
    private Person person;

    @CreationTimestamp
    private Date date;

    @ManyToOne
    private Address address;

    @Column(unique=true)
    @JsonIgnore
    private Long serviceOrderId;

    @ManyToMany
    private List<Product> products;

    @Min(value = 0, message = "Review should not be under the value of 0.")
    @Max(value = 5, message = "Review should not be above the value of 5.")
    private Integer riderReview;

    private String riderName;

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
