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

    @OneToOne(mappedBy = "shoppingCart")
    private Person person;

    @ManyToMany
    private List<Product> products;

    public ShoppingCart(List<Product> products) {
        this.products = products;
    }
    public ShoppingCart() {}
}
