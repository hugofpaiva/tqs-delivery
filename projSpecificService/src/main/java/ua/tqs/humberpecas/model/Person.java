package ua.tqs.humberpecas.model;

import lombok.Data;
import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Data
@Entity
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;
    private String pwd;
    private String email;

    @OneToMany
    private List<Purchase> purchases;

    @OneToMany
    private Set<Address> addresses;

    @OneToOne
    private ShoppingCart shoppingCart;

    public Person(String name, String pwd, String email, ShoppingCart sc) {
        this.name = name;
        this.pwd = pwd;
        this.email = email;
        this.shoppingCart = sc;
    }

    public Person() {}
}
