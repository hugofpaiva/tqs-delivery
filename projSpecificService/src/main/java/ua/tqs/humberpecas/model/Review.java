package ua.tqs.humberpecas.model;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

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
