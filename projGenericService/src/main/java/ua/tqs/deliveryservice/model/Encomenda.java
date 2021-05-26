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
    private Morada morada;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    private Condutor condutor;

    @ManyToOne
    private Loja loja;

    @Min(value = 0, message = "Review should not be under the value of 0.")
    @Max(value = 5, message = "Review should not be above the value of 5.")
    private int review_condutor;
}
