package ua.tqs.deliveryservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Data
@Entity
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    private String name;

    @JsonIgnore
    @Size(min = 8)
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
