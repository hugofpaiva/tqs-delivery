package ua.tqs.humberpecas.controller;


import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;
import ua.tqs.humberpecas.configuration.JwtRequestFilter;
import ua.tqs.humberpecas.configuration.WebSecurityConfig;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.Review;
import ua.tqs.humberpecas.services.HumberReviewService;

import java.io.IOException;

import static org.mockito.Mockito.*;

@WebMvcTest(value = HumberReviewController.class, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = WebSecurityConfig.class)})
@AutoConfigureMockMvc(addFilters = false)
public class HumberReviewControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private HumberReviewService service;

    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    @BeforeEach
    void setUp() throws IOException {
        RestAssuredMockMvc.mockMvc(mvc);

    }


    @ParameterizedTest
    @ValueSource(ints = {-1, 6} )
    @DisplayName("Insert a invalid Rating (number of stars) return HTTP status Bad Request")
    void whenInvalidNStars_thenReturnStatus400(int number) throws ResourceNotFoundException {

        Review r = new Review(1, number);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(r)
                .when()
                .post("/review/add")
                .then()
                .statusCode(400);

        verify(service, times(0)).addReview(r);
    }

    @Test
    @DisplayName("Insert a valid Rating (number of stars) return HTTP status OK")
    void whenValidReview_thenReturnOk() throws ResourceNotFoundException {
        Review review  = new Review(3, 5);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(review)
                .when()
                .post("/review/add")
                .then()
                .statusCode(200);

        verify(service, times(1)).addReview(review);

    }

    // TODO: alterar para rating de uma order invalida
    @Test
    @DisplayName("Review a invalid Order returs HTTP status Not Found")
    void whenInvalidReviewOrder_thenReturnStatus404() throws ResourceNotFoundException {
        Review review  = new Review(-1, 5);

        doThrow(ResourceNotFoundException.class).when(service).addReview(review);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(review)
                .when()
                .post("/review/add")
                .then()
                .statusCode(404);

        verify(service, times(1)).addReview(review);

    }


}
