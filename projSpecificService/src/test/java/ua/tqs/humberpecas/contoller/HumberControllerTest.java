package ua.tqs.humberpecas.contoller;


import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ua.tqs.humberpecas.model.Category;
import ua.tqs.humberpecas.model.Person;
import ua.tqs.humberpecas.model.Product;
import ua.tqs.humberpecas.service.HumberService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;


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
                new Product(0.50, "Parafuso", "xpto", 1000, false),
                new Product(5.00, "Chave inglesa", "xpto", 500, false)
        );

    }

    @Test
    public void whenGetAllProducts_thenReturnAllProducts(){


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
    public void whenGetProductsByCategory_thenReturnProducts(){

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
    public void whenValidRegister_thenReturnCrated(){

        Person p = new Person("Fernando", "12345678","fernando@ua.pt");

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
    public void whenInvalidRegister_thenReturnStatus400(String name, String pwd,String email){

        Person p = new Person(name, pwd, email);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(new Person(name, pwd, email))
                .when()
                .post("/shop/register")
                .then()
                .statusCode(400);

        verify(service, times(0)).register(p);


    }


    private static Stream<Arguments> invalidAccounts() {
        return Stream.of(
                arguments("Fernando", "12345", "fernando@ua.pt"),
                arguments("Fernando", "12345678", "fernandoua.pt"),
                arguments("","12345", "fernando@ua.pt")
        );

    }

}