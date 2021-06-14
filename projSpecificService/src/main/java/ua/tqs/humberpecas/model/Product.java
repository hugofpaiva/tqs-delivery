package ua.tqs.humberpecas.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    private Double price;

    private String name;

    @Enumerated(EnumType.STRING)
    private Category category;

    private String description;

    private String image_url;

    @ManyToMany(mappedBy = "products")
    private Set<Purchase> purchase;

    public Product(String name, double price, Category category, String description, String image_url) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.description = description;
        this.image_url = image_url;
    }


}
