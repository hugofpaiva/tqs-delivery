package ua.tqs.deliveryservice.model;

import javax.persistence.*;
import java.sql.Date;

@Entity
public class Compra {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @OneToOne(mappedBy = "id")
    private Utilizador id_consumidor;
    private Date data;

    @ManyToOne
    @JoinColumn(name="id", nullable = false)
    private Morada morada;

    private String status;

    @ManyToOne
    @JoinColumn(name="id", nullable = false)
    private Condutor condutor;

    private int review_condutor;
    private int review_compra;

    public void setId(Long id) { this.id = id; }

    @Id
    public Long getId() { return id; }
}

