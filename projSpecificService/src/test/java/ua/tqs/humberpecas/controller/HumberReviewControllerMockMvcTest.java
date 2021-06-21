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
import ua.tqs.humberpecas.exception.AccessNotAllowedException;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.exception.UnreachableServiceException;
import ua.tqs.humberpecas.model.Review;
import ua.tqs.humberpecas.service.HumberReviewService;

import java.io.IOException;

import static org.mockito.Mockito.*;

@WebMvcTest(value = HumberReviewController.class, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = WebSecurityConfig.class)})
@AutoConfigureMockMvc(addFilters = false)
public class HumberReviewControllerMockMvcTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private HumberReviewService service;

    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    private String userToken;

    @BeforeEach
    void setUp() throws IOException {
        RestAssuredMockMvc.mockMvc(mvc);
        this.userToken = "eyJhbGciOiJIUzI1NiJ9.eyJSb2xlIjoiQWRtaW4iLCJJc3N1ZXIiOiJJc3N1ZXIiLCJVc2VybmFtZSI6IkphdmFJblVzZSIsImV4cCI6MTYyMzYyMDQzMiwiaWF0IjoxNjIzNjIwNDMyfQ.Gib-gCJyL8-__G3zN4E-9VV1q75eYHZ8X6sS1WUNZB8";

    }


    @ParameterizedTest
    @ValueSource(ints = {-1, 6} )
    @DisplayName("Insert a invalid Rating (number of stars) return HTTP status Bad Request")
    void whenInvalidNStars_thenReturnStatus400(int number) throws ResourceNotFoundException, AccessNotAllowedException {

        Review r = new Review(1, number);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .header("authorization", "Bearer " + userToken)
                .body(r)
                .when()
                .post("/review/add")
                .then()
                .statusCode(400);

        verify(service, times(0)).addReview(r, userToken);
    }

    @Test
    @DisplayName("Insert a valid Rating (number of stars) return HTTP status OK")
    void whenValidReview_thenReturnOk() throws ResourceNotFoundException, AccessNotAllowedException {
        Review review  = new Review(3, 5);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .header("authorization", "Bearer " + userToken)
                .body(review)
                .when()
                .post("/review/add")
                .then()
                .statusCode(200);

        verify(service, times(1)).addReview(review, "Bearer " + userToken);

    }

    @Test
    @DisplayName("Review Order without user token throws HTTP status Bad Request")
    void whenOrderWithOutToken_thenThrowsStatus400() throws AccessNotAllowedException {

        Review r = new Review(1, 4);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(r)
                .when()
                .post("/review/add")
                .then()
                .statusCode(400);

        verify(service, times(0)).addReview(r, "Bearer " + userToken);
    }

    // TODO: alterar para rating de uma order invalida
    @Test
    @DisplayName("Review a invalid Order returs HTTP status Not Found")
    void whenInvalidReviewOrder_thenReturnStatus404() throws ResourceNotFoundException, AccessNotAllowedException {
        Review review  = new Review(-1, 5);

        doThrow(ResourceNotFoundException.class).when(service).addReview(review, "Bearer " + userToken);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .header("authorization", "Bearer " + userToken)
                .body(review)
                .when()
                .post("/review/add")
                .then()
                .statusCode(404);

        verify(service, times(1)).addReview(review, "Bearer " + userToken);

    }

    @Test
    @DisplayName("User id not corresponds to purchase Owner throws AccessNotAllowedException")
    void whenUserNotCorrespondsOwner_thenthenReturnStatus405() throws AccessNotAllowedException {

        Review review  = new Review(3, 5);

        doThrow(AccessNotAllowedException.class).when(service).addReview(review, "Bearer " + userToken);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .header("authorization", "Bearer " + userToken)
                .body(review)
                .when()
                .post("/review/add")
                .then()
                .statusCode(403);

        verify(service, times(1)).addReview(review, "Bearer " + userToken);

    }

    @Test
    @DisplayName("Error in communication with Delivery service throws UnreachableServiceExcption")
    void whenErrorInCommunication_thenThrowsStatusUnreachableService() throws AccessNotAllowedException {

        Review review  = new Review(-1, 5);

        doThrow(UnreachableServiceException.class).when(service).addReview(review, "Bearer " + userToken);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .header("authorization", "Bearer " + userToken)
                .body(review)
                .when()
                .post("/review/add")
                .then()
                .statusCode(500);

        verify(service, times(1)).addReview(review, "Bearer " + userToken);

    }

}
