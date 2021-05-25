package ua.tqs.humberpecas.model;

import lombok.Data;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Data
@Entity
public class Utilizador {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String nome;
    private String pwd;
    private String email;

    @OneToMany
    private List<Compra> compras;

    @OneToMany
    private Set<Morada> moradas;




}
