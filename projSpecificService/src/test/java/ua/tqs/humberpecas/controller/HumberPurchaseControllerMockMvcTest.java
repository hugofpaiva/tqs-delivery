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
import ua.tqs.humberpecas.model.*;
import ua.tqs.humberpecas.service.HumberPurchaseService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;


@WebMvcTest(value = HumberPurchaseController.class, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = WebSecurityConfig.class)})
@AutoConfigureMockMvc(addFilters = false)
class HumberPurchaseControllerMockMvcTest {
    private Product parafuso = new Product("Parafuso", 0.50, Category.SCREWS, "xpto",  "image_url");
    private Product chave = new Product("Chave inglesa", 5.00, Category.SCREWDRIVER, "xpto",  "image_url");

    private Purchase purchase = new Purchase(new Person(), new Address(), Arrays.asList(parafuso, chave));

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

    /* ******************************************
     *               GET ALL PURCHASES          *
     * ******************************************
     */

    @Test
    @DisplayName("User purchase list of invalid user returns HTTP UNAUTHORIZED")
    void testGetAllUserPurchases_whenInvalidToken_thenReturnStatus401() throws ResourceNotFoundException {


        when(service.getUserPurchases(0, 9, userToken)).thenThrow(InvalidLoginException.class);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .param("pageNo", 0)
                .param("pageSize", 9)
                .header("authorization",  userToken)
                .when()
                .get("/purchase/getAll")
                .then()
                .statusCode(401);

        verify(service, times(1)).getUserPurchases(0, 9, userToken);
    }

    @Test
    @DisplayName("User purchase list everything ok then return 200")
    void testGetAllUserPurchases_whenEverythingOK_thenReturnStatus200() throws ResourceNotFoundException {
        Map<String, Object> response = new HashMap<>();
        response.put("orders", purchase);
        response.put("currentPage", 0);
        response.put("totalItems", 1);
        response.put("totalPages", 1);
        response.put("reviewsGiven", 0);

        when(service.getUserPurchases(0, 9, userToken)).thenReturn(response);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .param("pageNo", 0)
                .param("pageSize", 9)
                .header("authorization",  userToken)
                .when()
                .get("/purchase/getAll")
                .then()
                .statusCode(200)
                .body("orders.products.size()", equalTo(2))
                .body("orders.products[0].name", equalTo(parafuso.getName()))
                .body("orders.products[0].category", equalTo(parafuso.getCategory().toString()))
                .body("orders.products[0].description", equalTo(parafuso.getDescription()))
                .body("orders.products[0].image_url", equalTo(parafuso.getImage_url()))
                .body("orders.products[1].name", equalTo(chave.getName()))
                .body("orders.products[1].category", equalTo(chave.getCategory().toString()))
                .body("orders.products[1].description", equalTo(chave.getDescription()))
                .body("orders.products[1].image_url", equalTo(chave.getImage_url()))
                .body("currentPage", equalTo(0))
                .body("totalItems", equalTo(1))
                .body("totalPages", equalTo(1));

        verify(service, times(1)).getUserPurchases(0, 9, userToken);
    }

    @Test
    @DisplayName("User purchase list with invalid pageNo returns HTTP BAD REQUEST")
    void testGetAllUserPurchases_whenPageNo_thenReturnStatus400() {

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .param("pageNo", -1)
                .param("pageSize", 9)
                .header("authorization",  userToken)
                .when()
                .get("/purchase/getAll")
                .then()
                .statusCode(400);

        verify(service, times(0)).getUserPurchases(0, 9, userToken);
    }

    @Test
    @DisplayName("User purchase list with invalid pageSize returns HTTP BAD REQUEST")
    void testGetAllUserPurchases_whenPageSize_thenReturnStatus400() {

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .param("pageNo", 0)
                .param("pageSize", 0)
                .header("authorization",  userToken)
                .when()
                .get("/purchase/getAll")
                .then()
                .statusCode(400);

        verify(service, times(0)).getUserPurchases(0, 9, userToken);
    }

    /* *******************************************
     *               MAKE NEW PURCHASE           *
     * *******************************************
     */
    @Test
    @DisplayName("Make Purchase with invalid user throws HTTP status Bad Request ")
    void whenPurchaseInvalidUser_thenThrowsStatus401() {
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
    @DisplayName("Make Purchase")
    void whenValidPurchase_thenReturnOk() {

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
    void whenPurchaseWithInvalidData_thenThrowsStatus404() {

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

    @Test
    @DisplayName("Error in communication with Delivery service throws UnreachableServiceException")
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

}
