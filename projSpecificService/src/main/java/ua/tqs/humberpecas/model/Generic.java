package ua.tqs.humberpecas.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Data
@Entity
public class Generic {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    private String name;

    @Column(unique = true)
    @JsonIgnore
    private String token;

    public Generic(String name, String token) {
        this.name = name;
        this.token = token;
    }

    public Generic() {}
}