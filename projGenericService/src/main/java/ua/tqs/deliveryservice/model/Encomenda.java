package ua.tqs.deliveryservice.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Date;

@Data
@Entity
public class Encomenda {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @CreationTimestamp
    private Date data;

    @ManyToOne
    @JoinColumn(name="id", nullable = false)
    private Morada morada;

    @OneToOne(mappedBy = "id")
    private Status status;

    @OneToOne(mappedBy = "id")
    private Condutor condutor;

    @OneToMany(mappedBy = "id")
    private Loja loja;

    @Min(value = 0, message = "Review should not be under the value of 0.")
    @Max(value = 5, message = "Review should not be above the value of 5.")
    private int review_condutor;
}
