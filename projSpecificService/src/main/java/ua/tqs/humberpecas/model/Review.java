package ua.tqs.humberpecas.model;


import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

public class Review {

    @NotBlank(message = "Order number is mandatory")
    private long orderId;


    @NotBlank(message = "Score is mandatory")
    @Min(0)
    @Max(5)
    private int numberStars;

    private String msg;


    public Review(long orderId, int numberStars){
        this.orderId = orderId;
        this.numberStars = numberStars;
    }

    public Review(){

    }

}
