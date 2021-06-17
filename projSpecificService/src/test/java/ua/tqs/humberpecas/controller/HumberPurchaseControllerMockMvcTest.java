package ua.tqs.humberpecas.controller;


import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;
import ua.tqs.humberpecas.configuration.JwtRequestFilter;
import ua.tqs.humberpecas.configuration.WebSecurityConfig;
import ua.tqs.humberpecas.dto.PurchaseDTO;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.PurchaseStatus;
import ua.tqs.humberpecas.service.HumberPurchaseService;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;


@WebMvcTest(value = HumberPurchaseController.class, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = WebSecurityConfig.class)})
@AutoConfigureMockMvc(addFilters = false)
class HumberPurchaseControllerMockMvcTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private HumberPurchaseService service;


    @MockBean
    private JwtRequestFilter jwtRequestFilter;


    private String userToken;

    @BeforeEach
    void setUp() throws IOException {
        RestAssuredMockMvc.mockMvc(mvc);

        userToken = "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDcwOTYwNDMsImlhdCI6MTYyMzA5OTI0MywiU3ViamVjdCI6Ikh1bWJlclBlY2FzIn0.oEZD63J134yUxHl658oSDJrw32BZcYHQbveZw8koAgP-2_d-8aH2wgJYJMlGnKIugOiI8H9Aa4OjPMWMUl9BFw";
    }

    /*
    @Test
    @DisplayName("User purchage list of invalid user returns HTTP status Not Found")
    void whenGetInvalidUserPurchages_thenReturnStatus404() throws ResourceNotFoundException {


        when(service.getUserPurchases(userToken)).thenThrow(new ResourceNotFoundException("Invalid User!"));

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .header("authorization", "Bearer " + userToken)
                .when()
                .get("/purchase/getAll?userId=1")
                .then()
                .statusCode(404);

        verify(service, times(1)).getUserPurchases(userToken);

    }
    */


    @Test
    @DisplayName("Make Purchage")
    void whenValidPurchage_thenReturnOk(){


        List<Long> productsId = Arrays.asList( Long.valueOf(6), Long.valueOf(7));

        PurchaseDTO p = new PurchaseDTO(Long.valueOf(1), new Date(), Long.valueOf(5) , productsId);


        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(p)
                .when()
                .post("/purchase/new")
                .then()
                .statusCode(200);

        verify(service, times(1)).newPurchase(p);


    }

    /*
    @Test
    @DisplayName("Get Status of an invalid Order return HTTP status Not Found ")
    void whenGetStatusInvalidOrder_thenReturnStatus404() throws ResourceNotFoundException {

        when(service.checkPurchaseStatus(anyLong())).thenThrow(new ResourceNotFoundException("Invalid Order !"));

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.TEXT)
                .when()
                .get("/purchase/status?orderId=0")
                .then()
                .statusCode(404);

        verify(service, times(1)).checkPurchaseStatus(0);
    }
    */




//    @Test
//    @DisplayName("User purchages list")
//    void whenGetUserPurchages_thenReturnPurchages() throws ResourceNotFoundException {
//
//        Person p = new Person("Fernando", "12345678","fernando@ua.pt");
//        List<Purchase> purchaseList = Arrays.asList(new Purchase(p, new Address(), ))
//
//        when(service.getUserPurchases(anyLong())).thenReturn();
//
//        RestAssuredMockMvc.given()
//                .contentType("application/json")
//                .when()
//                .get("/shop/purchageList?userId=1")
//                .then()
//                .statusCode(200)
//                .body("$.size()", Matchers.is(2))
//                .body("[0].products.size()", Matchers.is(2))
//                .body("[1].products.size()", Matchers.is(1))
//                .body("[0].person.email", Matchers.equalTo(p.getEmail()))
//                .body("[1].person.email", Matchers.equalTo(p.getEmail()));
//
//        verify(service, times(1)).getUserPurchases(1);
//
//    }




}
