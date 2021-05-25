package ua.tqs.deliveryservice.model;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Data
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private double preco;
    private String name;

    @ManyToOne
    private Categoria categoria;

    private String descricao;
    private long stockAtual;
    private boolean eliminado;

}
