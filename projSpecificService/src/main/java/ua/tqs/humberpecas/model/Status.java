package ua.tqs.humberpecas.model;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
public class Status {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String nome;

    @OneToMany
    private List<Compra> compras;
}
