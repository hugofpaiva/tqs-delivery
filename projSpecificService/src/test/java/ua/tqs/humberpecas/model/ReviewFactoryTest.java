package ua.tqs.humberpecas.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.*;

class ReviewFactoryTest {

    
    @Test
    public void whenStarsCorrect_thenReturnReview(){

        assertThat(ReviewFactory.createReview(4, "bom"), instanceOf(Review.class));

    }


    @ParameterizedTest
    @ValueSource(ints = { 6, -1 })
    public void whenStarsIncorrect_thenReturnIllegalArgumentException(int star){

        assertThat(ReviewFactory.createReview(star, "espetaculo!"), instanceOf(IllegalArgumentException.class));
    }

}