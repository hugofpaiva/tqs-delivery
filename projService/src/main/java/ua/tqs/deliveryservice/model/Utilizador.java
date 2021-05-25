package ua.tqs.deliveryservice.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Data
@Entity
public class Utilizador {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String nome;
    private String pwd;
    private String email;

    public void setId(Long id) { this.id = id; }

    @Id
    public Long getId() { return id; }


}
