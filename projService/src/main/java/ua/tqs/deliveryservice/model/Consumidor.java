package ua.tqs.deliveryservice.model;

import javax.persistence.*;

@Entity
public class Consumidor {
    @Id
    @OneToOne(mappedBy = "id")
    private Utilizador id_utilizador;

}
