package ua.tqs.deliveryservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.Date;
import java.util.List;

@Data
@Entity
public class Rider extends Person {

    private long reviewsSum;
    private int totalNumReviews;

    @CreationTimestamp
    private Date data_criacao_conta;
    
    @OneToMany(mappedBy = "rider")
    private List<Purchase> purchases;

    public Rider(String name, String pwd, String email) {
        super(name, pwd, email);
        reviewsSum = 0;
        totalNumReviews = 0;
    }

    public Rider() {}

}
