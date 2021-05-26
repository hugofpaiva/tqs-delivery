package ua.tqs.deliveryservice.model;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
public class Loja {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String nome;
    private String descricao;

    @OneToOne
    private Morada morada;
}
