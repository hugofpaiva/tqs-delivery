package ua.tqs.humberpecas.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @EqualsAndHashCode.Include
    private long id;

    private String name;

    @Size(min=8)
    private String pwd;

    @Email
    @Column(unique = true)
    @EqualsAndHashCode.Include
    private String email;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "person")
    @ToString.Exclude
    private List<Purchase> purchases;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "person")
    @ToString.Exclude
    private Set<Address> addresses;

    public Person(){ }


    public Person(String name, String pwd, String email){

        this.name = name;
        this.pwd = pwd;
        this.email = email;
        this.purchases = new ArrayList<>();
        this.addresses = new HashSet<>();
    }


}
