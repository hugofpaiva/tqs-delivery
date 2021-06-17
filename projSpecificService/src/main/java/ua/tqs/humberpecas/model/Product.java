package ua.tqs.humberpecas.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.HashSet;
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

    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @ToString.Exclude
    @ManyToMany(mappedBy = "products")
    private Set<Purchase> purchase;

    public Product(String name, double price, Category category, String description, String image_url) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.description = description;
        this.image_url = image_url;
        this.purchase = new HashSet<>();
    }

}
