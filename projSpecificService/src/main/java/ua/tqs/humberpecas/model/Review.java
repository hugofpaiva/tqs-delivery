package ua.tqs.humberpecas.model;


<<<<<<< HEAD
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
=======
import lombok.Data;
>>>>>>> ccbfbf3ad8cf20aaf68ee79fc0e4e9477a31ca70


import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;


@Data
public class Review {

<<<<<<< HEAD
    @NotBlank(message = "Order number is mandatory")
    private long orderId;


    @NotBlank(message = "Score is mandatory")
    @Min(0)
    @Max(5)
    private int numberStars;

    private String msg;
=======
    @NotNull
    private long orderId;


    @NotNull
    @Min(0)
    @Max(5)
    private int numberStars;
>>>>>>> ccbfbf3ad8cf20aaf68ee79fc0e4e9477a31ca70


    public Review(long orderId, int numberStars){
        this.orderId = orderId;
        this.numberStars = numberStars;
    }

    public Review(){ }

}
