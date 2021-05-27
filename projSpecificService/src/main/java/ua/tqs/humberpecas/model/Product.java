package ua.tqs.humberpecas.model;

import lombok.Data;
import javax.persistence.*;
import java.util.Set;

@Data
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private double price;
    private String name;

    @Enumerated(EnumType.STRING)
    private Category category;

    private String description;

    private long stock;
    private boolean deleted;

    @ManyToMany(mappedBy = "products")
    private Set<Purchase> purchase;
}
