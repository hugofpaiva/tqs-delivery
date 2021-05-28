package ua.tqs.humberpecas.model;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
public class ShoppingCart {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @OneToOne
    private Person person;

    @ManyToMany
    private List<Product> product;

}
