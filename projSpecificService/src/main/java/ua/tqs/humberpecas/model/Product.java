package ua.tqs.humberpecas.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;



    private Double price;

    private String name;

    @Enumerated(EnumType.STRING)
    private Category category;

    private String description;

    private long stock;

    private boolean deleted;

    @ManyToMany(mappedBy = "products")
    private Set<Purchase> purchase;



    public Product(double price, String name, String description, long stock, boolean deleted, Category category){
        this.price = price;
        this.name = name;
        this.description =  description;
        this.stock = stock;
        this.deleted = deleted;
        this.category = category;
    }


    public Product(String name, double price, Category category, String description, long stock) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.description = description;
        this.stock = stock;
        deleted = false;
    }

    public Product() {}

}
