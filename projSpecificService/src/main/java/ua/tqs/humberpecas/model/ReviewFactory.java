package ua.tqs.humberpecas.model;

public class ReviewFactory {


    public static Review createReview(int stars, String msg) throws IllegalArgumentException{

        if (stars > 5 || stars < 0){
            throw  new IllegalArgumentException("Invalid Review");
        }

        return new Review(stars, msg);

    }
}
