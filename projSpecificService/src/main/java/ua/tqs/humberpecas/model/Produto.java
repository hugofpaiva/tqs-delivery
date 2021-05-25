package ua.tqs.humberpecas.model;

import lombok.Data;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Data
@Entity
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private double preco;
    private String nome;

    @ManyToOne
    private Categoria categoria;

    private String descricao;

    private long stock;
    private boolean eliminado;

    @ManyToMany(mappedBy = "produtos")
    private Set<Compra> compra;
}
