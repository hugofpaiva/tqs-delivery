package ua.tqs.humberpecas.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Set;
/*
@Data
@Entity
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String nome;

    @OneToMany
    private Set<Produto> produtos;
}
*/

public enum Categoria {
    // ???
}
