package ua.tqs.humberpecas.model;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Set;

@Data
@Entity
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;


    private String name;

    @Size(min=8)
    private String pwd;


    @Email
    @Column(unique = true)
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



    public Person(){ }


    public Person(String name, String pwd, String email){

        this.name = name;
        this.pwd = pwd;
        this.email = email;
    }


}
