package ua.tqs.deliveryservice.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    private String name;

    private String pwd;

    @Column(unique=true)
    private String email;


    public Person(String name, String pwd, String email) {
        this.name = name;
        this.pwd = pwd;
        this.email = email;
    }

    public Person() { }
}
