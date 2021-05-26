package ua.tqs.deliveryservice.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.List;

@Data
@Entity
public class Condutor extends Utilizador {

    private double somatorio_reviews;
    private double num_total_reviews;

    @OneToMany
    private List<Encomenda> encomendas;
}
