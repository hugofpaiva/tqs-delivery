package ua.tqs.humberpecas.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class Morada {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String morada;
    private String codigo_postal;
    private String cidade;
    private String pais;

    @ManyToMany
    @JoinColumn(name = "id")
    private Utilizador utilizador;
}
