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

    @Column(unique = true)
    private String email;

    @OneToMany
    private List<Purchase> purchases;

    @OneToMany
    private Set<Address> addresses;

}
