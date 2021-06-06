package ua.tqs.humberpecas.model;


import lombok.Data;

@Data
public class Review {

    private long orderId;
    private int numberStars;
    private String msg;


    public Review(long orderId, int numberStars, String msg){
        this.orderId = orderId;
        this.numberStars = numberStars;
        this.msg = msg;
    }


}
