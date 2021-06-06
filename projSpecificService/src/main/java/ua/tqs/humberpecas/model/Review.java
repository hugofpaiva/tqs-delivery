package ua.tqs.humberpecas.model;


import lombok.Data;

@Data
public class Review {

    private long orderId;
    private int numberStars;
    private String msg;


}
