package ua.tqs.humberpecas.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private double preco;
    private String nome;

    @OneToOne(mappedBy = "id")
    private Categoria categoria;

    private String descricao;
    private int stock_atual;
    private boolean eliminado;

}
