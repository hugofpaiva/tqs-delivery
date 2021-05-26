package ua.tqs.humberpecas.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@Entity
public class Compra {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    // uma compra vai ter um utilizador; um utilizador vai ter varias compras
    @ManyToOne
    private Utilizador utilizador;

    @CreationTimestamp
    private Date data;

    // uma compra tem uma morada; uma morada tem varias compras
    @ManyToOne
    private Morada morada;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToMany
    private List<Produto> produtos;

}
