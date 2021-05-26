package ua.tqs.deliveryservice.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class Morada {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String morada;
    private String codigoPostal;
    private String cidade;
    private String pais;

    @OneToOne
    private Loja loja;
}
