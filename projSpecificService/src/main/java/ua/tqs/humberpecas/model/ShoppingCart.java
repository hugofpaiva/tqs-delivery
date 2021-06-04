package ua.tqs.humberpecas.model;

import ch.qos.logback.core.hook.ShutdownHook;
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
    private List<Product> products;

    public ShoppingCart(Person person, List<Product> products) {
        this.person = person;
        this.products = products;
    }
    public ShoppingCart() {}
}
