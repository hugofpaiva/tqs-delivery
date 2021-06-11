package ua.tqs.humberpecas.model;


import lombok.Data;


import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;


@Data
public class Review {

    @NotNull
    private long orderId;


    @NotNull
    @Min(0)
    @Max(5)
    private int numberStars;


    public Review(long orderId, int numberStars){
        this.orderId = orderId;
        this.numberStars = numberStars;
    }

    public Review(){ }

}
