package ua.tqs.humberpecas.model;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;


@Data
public class Review implements Serializable {

    @NotNull
    private long orderId;


    @NotNull
    @Min(0)
    @Max(5)
    private Integer review;


    public Review(long orderId, int review){
        this.orderId = orderId;
        this.review = review;
    }

    public Review(){ }

}
