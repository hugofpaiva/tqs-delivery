package ua.tqs.deliveryservice.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class Utilizador {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String nome;
    private String pwd;
    private String email;


}
