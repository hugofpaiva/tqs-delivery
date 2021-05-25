package ua.tqs.deliveryservice.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Data
@Entity
public class Condutor {
    @Id
    @OneToOne(mappedBy = "id")
    private Utilizador utilizador;

    private double sumatorio_reviews;
    private double num_total_reviews;
}
