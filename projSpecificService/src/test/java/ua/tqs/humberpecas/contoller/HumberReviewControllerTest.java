package ua.tqs.humberpecas.contoller;


import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ua.tqs.humberpecas.model.Review;
import ua.tqs.humberpecas.service.HumberService;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

@WebMvcTest(HumberController.class)
public class HumberReviewControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private HumberService service;

    @BeforeEach
    void setUp() throws IOException {
        RestAssuredMockMvc.mockMvc(mvc);

    }


    @ParameterizedTest
    @ValueSource(ints = {-1, 6} )
    @DisplayName("Insert a invalid Rating (number of stars) return HTTP status Bad Request")
    void whenInvalidNStars_thenReturnStatus400(int number){

        Review r = new Review(1, number);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(r)
                .when()
                .post("/shop/newReview")
                .then()
                .statusCode(400);

        verify(service, times(0)).addReview(r);
    }

    @Test
    @DisplayName("Insert a valid Rating (number of stars) return HTTP status OK")
    void whenValidReview_thenReturnOk(){
        Review review  = new Review(3, 5);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(review)
                .when()
                .post("/shop/newReview")
                .then()
                .statusCode(200);

        verify(service, times(1)).addReview(review);

    }

    // TODO: alterar para rating de uma order invalida
    @Test
    @DisplayName("Review a invalid Order returs HTTP status Not Found")
    void whenInvalidReviewOrder_thenReturnStatus400(){
        Review review  = new Review(-1, 5);

        doThrow(IllegalArgumentException.class).when(service).addReview(review);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(review)
                .when()
                .post("/shop/newReview")
                .then()
                .statusCode(400);

        verify(service, times(1)).addReview(review);

    }

    private static Stream<Arguments> invalidAccounts() {
        return Stream.of(
                arguments("Fernando", "12345", "fernando@ua.pt"),
                arguments("Fernando", "12345678", "fernandoua.pt"),
                arguments("","12345", "fernando@ua.pt")
        );

    }
}
