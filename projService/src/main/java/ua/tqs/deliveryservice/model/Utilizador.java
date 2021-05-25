package ua.tqs.deliveryservice.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;


@Entity
public class Utilizador {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String nome;
    private int id_loja;
    private String pwd;
    private String email;

    public Utilizador() {}

    public void setId(Long id) { this.id = id; }

    @Id
    public Long getId() { return id; }


}
