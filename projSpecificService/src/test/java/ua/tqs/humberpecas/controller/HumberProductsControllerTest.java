package ua.tqs.humberpecas.controller;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.hamcrest.Matchers;
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
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.Category;
import ua.tqs.humberpecas.model.Product;
import ua.tqs.humberpecas.services.HumberProductService;


import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;


@WebMvcTest(value = HumberProductsController.class, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = WebSecurityConfig.class)})
@AutoConfigureMockMvc(addFilters = false)
class HumberProductsControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private HumberProductService service;

    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    private List<Product> catalog;

    @BeforeEach
    void setUp() throws IOException {
        RestAssuredMockMvc.mockMvc(mvc);

        catalog = Arrays.asList(
                new Product(0.50, "Parafuso", "xpto", 1000, false, Category.PARAFUSOS ),
                new Product(5.00, "Chave inglesa", "xpto", 500, false, Category.CHAVES)
        );

    }

    @Test
    @DisplayName("Get list of All Products")
    void whenGetAllProducts_thenReturnAllProducts(){

        when(service.getCatolog()).thenReturn(catalog);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .when()
                .get("/product/getAll")
                .then()
                .statusCode(200)
                .body("$.size()", Matchers.equalTo(2))
                .body("[0].name", Matchers.equalTo("Parafuso"))
                .body("[1].name", Matchers.equalTo("Chave inglesa"));

        verify(service, times(1)).getCatolog();

    }

    @Test
    @DisplayName("Get Specific Product (details)")
    void whenGetValidProduct_thenReturnProduct() throws ResourceNotFoundException {

        when(service.getProductById(anyLong())).thenReturn(catalog.get(0));

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .when()
                .get("/product/get/1")
                .then()
                .statusCode(200)
                .body("name", Matchers.equalTo("Parafuso"));

        verify(service, times(1)).getProductById(1);

    }

    @Test
    @DisplayName("Get an Inexistent Product return HTTP status Not Found")
    void whenGetInvalidProduct_thenReturnStatus404() throws ResourceNotFoundException {

        when(service.getProductById(anyLong())).thenThrow(new ResourceNotFoundException("Product not found"));

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .when()
                .get("/product/get/1")
                .then()
                .statusCode(404);

        verify(service, times(1)).getProductById(1);
    }


    @Test
    @DisplayName("Get Product by Category")
    void whenGetProductsByCategory_thenReturnProducts() throws ResourceNotFoundException {

        when(service.getProductsByCategory(Category.CHAVES)).thenReturn(catalog.subList(1,2));

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .when()
                .get("/product/getAll?category=CHAVES")
                .then()
                .statusCode(200)
                .body("$.size()", Matchers.equalTo(1))
                .body("[0].name", Matchers.equalTo("Chave inglesa"));

        verify(service, times(1)).getProductsByCategory(Category.CHAVES);

    }



}
