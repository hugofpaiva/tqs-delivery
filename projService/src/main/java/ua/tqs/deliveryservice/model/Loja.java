package ua.tqs.deliveryservice.model;

import lombok.Data;
import javax.persistence.*;
import java.util.List;

@Data
@Entity
public class Loja {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String nome;
    private String descricao;
    private String morada;
    private String codigoPostal;
    private String pais;
    private String cidade;
    private long somaReviews;
    private long totalReviews;

    @ManyToOne
    private List<Empresario> empresarios;

}
