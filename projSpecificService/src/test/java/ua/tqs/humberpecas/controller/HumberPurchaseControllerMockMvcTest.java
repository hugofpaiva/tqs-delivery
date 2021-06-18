package ua.tqs.humberpecas.controller;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
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
import ua.tqs.humberpecas.exception.AccessNotAllowedException;
import ua.tqs.humberpecas.exception.InvalidLoginException;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.exception.UnreachableServiceException;
import ua.tqs.humberpecas.service.HumberPurchaseService;

import java.util.Arrays;
import java.util.List;

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
    private PurchaseDTO purchaseDTO;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mvc);
        userToken = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDcwOTYwNDMsImlhdCI6MTYyMzA5OTI0MywiU3ViamVjdCI6Ikh1bWJlclBlY2FzIn0.oEZD63J134yUxHl658oSDJrw32BZcYHQbveZw8koAgP-2_d-8aH2wgJYJMlGnKIugOiI8H9Aa4OjPMWMUl9BFw";
        List<Long> productsId = Arrays.asList(Long.valueOf(6), Long.valueOf(7));

        purchaseDTO = new PurchaseDTO(Long.valueOf(5), productsId);

    }

    /*
    @Test
    @DisplayName("User purchage list of invalid user returns HTTP status Not Found")
    void whenGetInvalidUserPurchages_thenReturnStatus404() throws ResourceNotFoundException {


        when(service.getUserPurchases(this.userToken)).thenThrow(new ResourceNotFoundException("Invalid User!"));

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .header("authorization",  userToken)
                .when()
                .get("/purchase/getAll?userId=1")
                .then()
                .statusCode(404);

        verify(service, times(1)).getUserPurchases(userToken);

    }
    */

    @Test
    @DisplayName("Make Purchase with invalid user throws HTTP status Bad Request ")
    void whenPuchageInvalidUser_thenThrowsStatus401() {
        when(service.newPurchase(purchaseDTO, userToken)).thenThrow(InvalidLoginException.class);


        RestAssuredMockMvc.given()
                .contentType("application/json")
                .header("authorization", userToken)
                .body(purchaseDTO)
                .when()
                .post("/purchase/new")
                .then()
                .statusCode(401);

        verify(service, times(1)).newPurchase(purchaseDTO, userToken);

    }


    @Test
    @DisplayName("Make Purchase without user token throws HTTP status Bad Request")
    void whenPurchaseWithOutToken_thenThrowsStatus400() throws AccessNotAllowedException {

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(purchaseDTO)
                .when()
                .post("/purchase/new")
                .then()
                .statusCode(400);

        verify(service, times(0)).newPurchase(purchaseDTO, userToken);
    }

    @Test
    @DisplayName("Make Purchage")
    void whenValidPurchage_thenReturnOk() {

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .header("authorization", userToken)
                .body(purchaseDTO)
                .when()
                .post("/purchase/new")
                .then()
                .statusCode(200);

        verify(service, times(1)).newPurchase(purchaseDTO, userToken);


    }

    @Test
    @DisplayName("Make Purchase with Invalid Data throws HTTP status ResourseNotFound")
    void whenPurchaseWithInvalidData_thenthenThrowsStatus404() {

        when(service.newPurchase(purchaseDTO, userToken)).thenThrow(ResourceNotFoundException.class);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .header("authorization", userToken)
                .body(purchaseDTO)
                .when()
                .post("/purchase/new")
                .then()
                .statusCode(404);

        verify(service, times(1)).newPurchase(purchaseDTO, userToken);

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


    @Test
    @DisplayName("Error in communication with Delivery service throws UnreachableServiceExcption")
    void whenErrorInCommunication_thenThrowsStatusUnreachableService() throws AccessNotAllowedException {
        when(service.newPurchase(purchaseDTO, userToken)).thenThrow(UnreachableServiceException.class);


        RestAssuredMockMvc.given()
                .contentType("application/json")
                .header("authorization", userToken)
                .body(purchaseDTO)
                .when()
                .post("/purchase/new")
                .then()
                .statusCode(500);

        verify(service, times(1)).newPurchase(purchaseDTO, userToken);

    }

    /*

    @Test
    @DisplayName("Get Order Status")
    void whenGetOrderStatus_thenReturnStatus() throws ResourceNotFoundException {

        when(service.checkPurchaseStatus(anyLong())).thenReturn(PurchaseStatus.PENDENT);

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.TEXT)
                .when()
                .get("/purchase/status?orderId=0")
                .then()
                .statusCode(200)
                .body(Matchers.equalTo(PurchaseStatus.PENDENT.getStatus()) );

        verify(service, times(1)).checkPurchaseStatus(0);
    }
*/
}
