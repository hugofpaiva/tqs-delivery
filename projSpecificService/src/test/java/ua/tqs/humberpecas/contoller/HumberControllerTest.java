package ua.tqs.humberpecas.contoller;


import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.parsing.Parser;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;
import ua.tqs.humberpecas.dto.AddressDTO;
import ua.tqs.humberpecas.dto.PersonDTO;
import ua.tqs.humberpecas.dto.PurchageDTO;
import ua.tqs.humberpecas.execption.ResourceNotFoundException;
import ua.tqs.humberpecas.model.*;
import ua.tqs.humberpecas.service.HumberService;
import io.restassured.parsing.Parser;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;


import static io.restassured.RestAssured.given;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;


@WebMvcTest(HumberController.class)
class HumberControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private HumberService service;


    private List<Product> catalog;


    @BeforeEach
    void setUp() throws IOException {
        RestAssuredMockMvc.mockMvc(mvc);

        catalog = Arrays.asList(
                new Product(0.50, "Parafuso", "xpto", 1000, false,Category.PARAFUSOS ),
                new Product(5.00, "Chave inglesa", "xpto", 500, false, Category.CHAVES)
        );

    }

    @Test
    void whenGetAllProducts_thenReturnAllProducts(){


        when(service.getCatolog()).thenReturn(catalog);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .when()
                .get("/shop/products")
                .then()
                .statusCode(200)
                .body("$.size()", Matchers.equalTo(2))
                .body("[0].name", Matchers.equalTo("Parafuso"))
                .body("[1].name", Matchers.equalTo("Chave inglesa"));

        verify(service, times(1)).getCatolog();

    }

    @Test
    void whenGetValidProduct_thenReturnProduct() throws ResourceNotFoundException {

        when(service.getProductById(anyLong())).thenReturn(catalog.get(0));

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .when()
                .get("/shop/products/1")
                .then()
                .statusCode(200)
                .body("name", Matchers.equalTo("Parafuso"));

        verify(service, times(1)).getProductById(1);

    }


    @Test
    void whenGetInvalidProduct_thenReturnStatus404() throws ResourceNotFoundException {

        when(service.getProductById(anyLong())).thenThrow(new ResourceNotFoundException("Product not found"));

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .when()
                .get("/shop/products/1")
                .then()
                .statusCode(404);

        verify(service, times(1)).getProductById(1);

    }


    @ParameterizedTest
    @ValueSource(ints = {-1, 6} )
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

    // TODO: Corrigir enum json serialization

    @Test
    void whenGetOrderStatus_thenReturnStatus() throws ResourceNotFoundException {

        when(service.checkPurchageStatus(anyLong())).thenReturn(PurchageStatus.PENDENT);

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.TEXT)
                .when()
                .get("/shop/order?orderId=0")
                .then()
                .statusCode(200)
                .body(Matchers.equalTo(PurchageStatus.PENDENT.getStatus()) );

        verify(service, times(1)).checkPurchageStatus(0);
    }

    @Test
    void whenGetStatusInvalidOrder_thenReturnStatus400() throws ResourceNotFoundException {

        when(service.checkPurchageStatus(anyLong())).thenThrow(new ResourceNotFoundException("Invalid Order !"));

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.TEXT)
                .when()
                .get("/shop/order?orderId=0")
                .then()
                .statusCode(400);

        verify(service, times(1)).checkPurchageStatus(0);
    }


    @Test
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


    @Test
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



    @Test
    void whenGetProductsByCategory_thenReturnProducts(){

        when(service.getProductsByCategory(Category.CHAVES)).thenReturn(catalog.subList(1,2));

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .when()
                .get("/shop/products?category=CHAVES")
                .then()
                .statusCode(200)
                .body("$.size()", Matchers.equalTo(1))
                .body("[0].name", Matchers.equalTo("Chave inglesa"));

        verify(service, times(1)).getProductsByCategory(Category.CHAVES);

    }

    @Test
    void whenValidRegister_thenReturnCrated(){

        List<AddressDTO> addresses = Arrays.asList(new AddressDTO("Aveiro", "3730-123","Aveiro","Portugal"));

        PersonDTO p = new PersonDTO("Fernando", "12345678","fernando@ua.pt", addresses);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(p)
                .when()
                .post("/shop/register")
                .then()
                .statusCode(201);

        verify(service, times(1)).register(p);

    }


    @ParameterizedTest
    @MethodSource("invalidAccounts")
    void whenInvalidRegister_thenReturnStatus400(String name, String pwd,String email){

        List<AddressDTO> addresses = Arrays.asList(new AddressDTO("Aveiro", "3730-123","Aveiro","Portugal"));

        PersonDTO p = new PersonDTO(name, pwd, email, addresses);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(new Person(name, pwd, email))
                .when()
                .post("/shop/register")
                .then()
                .statusCode(400);

        verify(service, times(0)).register(p);


    }


    @Test
    void whenUserAlreadyExists_thenReturnStatus409(){

        List<AddressDTO> addresses = Arrays.asList(new AddressDTO("Aveiro", "3730-123","Aveiro","Portugal"));

        PersonDTO p = new PersonDTO("Fernando", "12345678","fernando@ua.pt", addresses);
        doThrow(new DataIntegrityViolationException("User alerady exists!")).when(service).register(p);
        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(p)
                .when()
                .post("/shop/register")
                .then()
                .statusCode(409);

        verify(service, times(1)).register(p);

    }


    private static Stream<Arguments> invalidAccounts() {
        return Stream.of(
                arguments("Fernando", "12345", "fernando@ua.pt"),
                arguments("Fernando", "12345678", "fernandoua.pt"),
                arguments("","12345", "fernando@ua.pt")
        );

    }

}