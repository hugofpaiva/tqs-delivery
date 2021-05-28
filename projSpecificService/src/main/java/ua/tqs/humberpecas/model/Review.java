package ua.tqs.humberpecas.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Review {

    private int numberStars;
    private String msg;

    public Review(int numberStars, String msg){
        this.numberStars = numberStars;
        this.msg = msg;
    }

}
