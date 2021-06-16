package ua.tqs.humberpecas.model;

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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private Double price;

    private String name;

    @Enumerated(EnumType.STRING)
    private Category category;

    private String description;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToMany(mappedBy = "products")
    private Set<Purchase> purchase;



    public Product(double price, String name, String description, Category category){
        this.price = price;
        this.name = name;
        this.description =  description;
        this.category = category;
        this.purchase = new HashSet<>();
    }




}
