package ua.tqs.humberpecas.contoller;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ua.tqs.humberpecas.dto.PurchageDTO;
import ua.tqs.humberpecas.execption.ResourceNotFoundException;
import ua.tqs.humberpecas.model.Category;
import ua.tqs.humberpecas.model.Product;
import ua.tqs.humberpecas.service.HumberService;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;


@WebMvcTest(HumberController.class)
public class HumberPurchagesControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private HumberService service;


    @BeforeEach
    void setUp() throws IOException {
        RestAssuredMockMvc.mockMvc(mvc);

    }

    @Test
    @DisplayName("User purchage list of invalid user returns HTTP status Bad Request")
    void whenGetInvalidUserPurchages_thenReturnStatus400() throws ResourceNotFoundException {


        when(service.getUserPurchases(anyLong())).thenThrow(new ResourceNotFoundException("Invalid User !"));

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .when()
                .get("/shop/purchageList?userId=1")
                .then()
                .statusCode(400);

        verify(service, times(1)).getUserPurchases(1);

    }



    @Test
    @DisplayName("Make Purchage")
    void whenValidPurchage_thenReturnOk(){


        List<Long> productsId = Arrays.asList( Long.valueOf(6), Long.valueOf(7));

        PurchageDTO p = new PurchageDTO(Long.valueOf(1), new Date(), Long.valueOf(5) , productsId);


        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(p)
                .when()
                .post("/shop/purchage")
                .then()
                .statusCode(200);

        verify(service, times(1)).newPurchase(p);


    }



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
