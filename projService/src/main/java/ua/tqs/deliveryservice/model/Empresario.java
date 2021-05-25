package ua.tqs.deliveryservice.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Data
@Entity
public class Empresario extends Utilizador {

    @OneToMany(mappedBy = "empresarios")
    private Loja loja;


}
