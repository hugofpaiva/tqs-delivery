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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private Double price;

    private String name;

    @Enumerated(EnumType.STRING)
    private Category category;

    private String description;

    @ManyToMany(mappedBy = "products")
    private Set<Purchase> purchase;



    public Product(double price, String name, String description, Category category){
        this.price = price;
        this.name = name;
        this.description =  description;
        this.category = category;
    }




}
