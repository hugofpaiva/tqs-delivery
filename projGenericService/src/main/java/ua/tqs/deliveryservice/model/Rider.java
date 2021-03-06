package ua.tqs.deliveryservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class Rider extends Person {
    @JsonIgnore
    private long reviewsSum;

    @JsonIgnore
    private int totalNumReviews;

    @CreationTimestamp
    private Date data_criacao_conta;
    
    @OneToMany(mappedBy = "rider")
    @JsonIgnore
    private List<Purchase> purchases;

    public Rider(String name, String pwd, String email) {
        super(name, pwd, email);
        reviewsSum = 0;
        totalNumReviews = 0;
    }

    public Rider() {}



}
