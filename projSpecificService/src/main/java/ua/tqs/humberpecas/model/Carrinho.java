package ua.tqs.humberpecas.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@Entity
public class Carrinho {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @OneToOne
    private Utilizador utilizador;

    @ManyToMany
    private List<Produto> produtos;

}
