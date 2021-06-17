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
import ua.tqs.humberpecas.model.Category;
import ua.tqs.humberpecas.model.Product;
import ua.tqs.humberpecas.service.HumberProductService;


import java.io.IOException;
import java.util.*;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;


@WebMvcTest(value = HumberProductsController.class, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = WebSecurityConfig.class)})
@AutoConfigureMockMvc(addFilters = false)
class HumberProductsControllerMockMvcTest {
    private Product parafuso = new Product("Parafuso", 0.50, Category.SCREWS, "xpto",  "image_url");
    private Product chave = new Product("Chave inglesa", 5.00, Category.SCREWDRIVER, "xpto",  "image_url");
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
    }

    // -------------------------------
    // --      GET ALL PRODUCTS     --
    // -------------------------------

    @Test
    @DisplayName("Get list of All Products: invalid pageNo then BAD_REQUEST")
    void testGetAllProducts_whenInvalidPageNo_thenReturnBadRequest(){
        RestAssuredMockMvc.given()
                .contentType("application/json")
                .param("pageNo", -1)
                .when()
                .get("/product/getAll")
                .then()
                .statusCode(400);

        verify(service, times(0)).getProductsFiltered(anyInt(), anyInt(), anyString(), any(), any(), anyString(), any());
    }

    @Test
    @DisplayName("Get list of All Products: invalid pageSize then BAD_REQUEST")
    void testGetAllProducts_whenInvalidPageSize_thenReturnBadRequest(){
        RestAssuredMockMvc.given()
                .contentType("application/json")
                .param("pageSize", 0)
                .when()
                .get("/product/getAll")
                .then()
                .statusCode(400);

        verify(service, times(0)).getProductsFiltered(anyInt(), anyInt(), anyString(), any(), any(), anyString(), any());
    }

    @Test
    @DisplayName("Get list of All Products: invalid minPrice then BAD_REQUEST")
    void testGetAllProducts_whenInvalidMinPrice_thenReturnBadRequest(){
        RestAssuredMockMvc.given()
                .contentType("application/json")
                .param("minPrice", -2)
                .when()
                .get("/product/getAll")
                .then()
                .statusCode(400);

        verify(service, times(0)).getProductsFiltered(anyInt(), anyInt(), anyString(), any(), any(), anyString(), any());
    }

    @Test
    @DisplayName("Get list of All Products: invalid maxPrice then BAD_REQUEST")
    void testGetAllProducts_whenInvalidMaxPrice_thenReturnBadRequest(){
        RestAssuredMockMvc.given()
                .contentType("application/json")
                .param("maxPrice", -2)
                .when()
                .get("/product/getAll")
                .then()
                .statusCode(400);

        verify(service, times(0)).getProductsFiltered(anyInt(), anyInt(), anyString(), any(), any(), anyString(), any());
    }

    @Test
    @DisplayName("Get list of All Products: maxPrice < minPrice then BAD_REQUEST")
    void testGetAllProducts_whenMaxPriceSmallerThanMinPrice_thenReturnBadRequest(){
        RestAssuredMockMvc.given()
                .contentType("application/json")
                .param("maxPrice", 5)
                .param("minPrice", 10)
                .when()
                .get("/product/getAll")
                .then()
                .statusCode(400);

        verify(service, times(0)).getProductsFiltered(anyInt(), anyInt(), anyString(), any(), any(), anyString(), any());
    }

    @Test
    @DisplayName("Get list of All Products: sort by price then return")
    void testGetAllProducts_whenSortByPrice_thenReturn(){
        List<Product> responseList = new ArrayList<>();
        responseList.add(parafuso);
        responseList.add(chave);

        Map<String, Object> response = new HashMap<>();
        response.put("products", responseList);
        response.put("currentPage", 0);
        response.put("totalItems", 2);
        response.put("totalPages", 1);

        when(service.
                getProductsFiltered(0, 9, null, 100000.0, 0.0, "price", null)).
                thenReturn(response);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .param("orderBy", "price")
                .when()
                .get("/product/getAll")
                .then()
                .statusCode(200)
                .body("products.size()", equalTo(2))
                .body("products[0].name", equalTo(parafuso.getName()))
                .body("products[0].category", equalTo(parafuso.getCategory().toString()))
                .body("products[0].description", equalTo(parafuso.getDescription()))
                .body("products[0].image_url", equalTo(parafuso.getImage_url()))
                .body("products[1].name", equalTo(chave.getName()))
                .body("products[1].category", equalTo(chave.getCategory().toString()))
                .body("products[1].description", equalTo(chave.getDescription()))
                .body("products[1].image_url", equalTo(chave.getImage_url()))
                .body("currentPage", equalTo(0))
                .body("totalItems", equalTo(2))
                .body("totalPages", equalTo(1));


        verify(service, times(1)).
                getProductsFiltered(0, 9, null, 100000.0, 0.0, "price", null);
    }

    @Test
    @DisplayName("Get list of All Products: filter by name")
    void testGetAllProducts_whenFilterByName_thenReturn(){
        List<Product> responseList = new ArrayList<>();
        responseList.add(parafuso);

        Map<String, Object> response = new HashMap<>();
        response.put("products", responseList);
        response.put("currentPage", 0);
        response.put("totalItems", 1);
        response.put("totalPages", 1);

        when(service.
                getProductsFiltered(0, 9, parafuso.getName(), 100000.0, 0.0, null, null)).
                thenReturn(response);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .param("name", parafuso.getName())
                .when()
                .get("/product/getAll")
                .then()
                .statusCode(200)
                .body("products.size()", equalTo(1))
                .body("products[0].name", equalTo(parafuso.getName()))
                .body("products[0].category", equalTo(parafuso.getCategory().toString()))
                .body("products[0].description", equalTo(parafuso.getDescription()))
                .body("products[0].image_url", equalTo(parafuso.getImage_url()))
                .body("currentPage", equalTo(0))
                .body("totalItems", equalTo(1))
                .body("totalPages", equalTo(1));


        verify(service, times(1)).
                getProductsFiltered(0, 9, parafuso.getName(), 100000.0, (double)0.0, null, null);
    }

    @Test
    @DisplayName("Get list of All Products: filter by name and category")
    void testGetAllProducts_whenFilterByNameAndCategory_thenReturn(){
        List<Product> responseList = new ArrayList<>();
        responseList.add(chave);

        Map<String, Object> response = new HashMap<>();
        response.put("products", responseList);
        response.put("currentPage", 0);
        response.put("totalItems", 1);
        response.put("totalPages", 1);

        when(service.
                getProductsFiltered(0, 9, chave.getName(), 100000.0, 0.0, null, chave.getCategory())).
                thenReturn(response);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .param("name", chave.getName())
                .param("category", chave.getCategory())
                .when()
                .get("/product/getAll")
                .then()
                .statusCode(200)
                .body("products.size()", equalTo(1))
                .body("products[0].name", equalTo(chave.getName()))
                .body("products[0].category", equalTo(chave.getCategory().toString()))
                .body("products[0].description", equalTo(chave.getDescription()))
                .body("products[0].image_url", equalTo(chave.getImage_url()))
                .body("currentPage", equalTo(0))
                .body("totalItems", equalTo(1))
                .body("totalPages", equalTo(1));


        verify(service, times(1)).
                getProductsFiltered(0, 9, chave.getName(), 100000.0, 0.0, null, chave.getCategory());
    }

    @Test
    @DisplayName("Get list of All Products: filter by minPrice")
    void testGetAllProducts_whenFilterByMinPrice_thenReturn(){
        List<Product> responseList = new ArrayList<>();
        responseList.add(chave);

        Map<String, Object> response = new HashMap<>();
        response.put("products", responseList);
        response.put("currentPage", 0);
        response.put("totalItems", 1);
        response.put("totalPages", 1);

        when(service.
                getProductsFiltered(0, 9, null, 100000.0, 3.0, null, null)).
                thenReturn(response);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .param("minPrice", 3)
                .when()
                .get("/product/getAll")
                .then()
                .statusCode(200)
                .body("products.size()", equalTo(1))
                .body("products[0].name", equalTo(chave.getName()))
                .body("products[0].category", equalTo(chave.getCategory().toString()))
                .body("products[0].description", equalTo(chave.getDescription()))
                .body("products[0].image_url", equalTo(chave.getImage_url()))
                .body("currentPage", equalTo(0))
                .body("totalItems", equalTo(1))
                .body("totalPages", equalTo(1));


        verify(service, times(1)).
                getProductsFiltered(0, 9, null, 100000.0, 3.0, null, null);
    }

    @Test
    @DisplayName("Get list of All Products: filter by minPrice and maxPrice")
    void testGetAllProducts_whenFilterByMinPriceAndMaxPrice_thenReturn(){
        List<Product> responseList = new ArrayList<>();
        responseList.add(parafuso);

        Map<String, Object> response = new HashMap<>();
        response.put("products", responseList);
        response.put("currentPage", 0);
        response.put("totalItems", 1);
        response.put("totalPages", 1);

        when(service.
                getProductsFiltered(0, 9, null, 1.0, 0.0, null, null)).
                thenReturn(response);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .param("minPrice", 0)
                .param("maxPrice", 1)
                .when()
                .get("/product/getAll")
                .then()
                .statusCode(200)
                .body("products.size()", equalTo(1))
                .body("products[0].name", equalTo(parafuso.getName()))
                .body("products[0].category", equalTo(parafuso.getCategory().toString()))
                .body("products[0].description", equalTo(parafuso.getDescription()))
                .body("products[0].image_url", equalTo(parafuso.getImage_url()))
                .body("currentPage", equalTo(0))
                .body("totalItems", equalTo(1))
                .body("totalPages", equalTo(1));


        verify(service, times(1)).
                getProductsFiltered(0, 9, null, 1.0, 0.0, null, null);
    }

    @Test
    @DisplayName("Get list of All Products: no filter")
    void testGetAllProducts_whenNoFilter_thenReturn(){
        List<Product> responseList = new ArrayList<>();
        responseList.add(chave);
        responseList.add(parafuso);


        Map<String, Object> response = new HashMap<>();
        response.put("products", responseList);
        response.put("currentPage", 0);
        response.put("totalItems", 2);
        response.put("totalPages", 1);

        when(service.
                getProductsFiltered(0, 9, null, 100000.0, 0.0, null, null)).
                thenReturn(response);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .when()
                .get("/product/getAll")
                .then()
                .statusCode(200)
                .body("products.size()", equalTo(2))
                .body("products[0].name", equalTo(chave.getName()))
                .body("products[0].category", equalTo(chave.getCategory().toString()))
                .body("products[0].description", equalTo(chave.getDescription()))
                .body("products[0].image_url", equalTo(chave.getImage_url()))
                .body("products[1].name", equalTo(parafuso.getName()))
                .body("products[1].category", equalTo(parafuso.getCategory().toString()))
                .body("products[1].description", equalTo(parafuso.getDescription()))
                .body("products[1].image_url", equalTo(parafuso.getImage_url()))
                .body("currentPage", equalTo(0))
                .body("totalItems", equalTo(2))
                .body("totalPages", equalTo(1));


        verify(service, times(1)).
                getProductsFiltered(0, 9, null, 100000.0, 0.0, null, null);
    }

    @Test
    @DisplayName("Get list of All Products: no products")
    void testGetAllProducts_whenNoProducts_thenReturn(){
        List<Product> responseList = new ArrayList<>();


        Map<String, Object> response = new HashMap<>();
        response.put("products", responseList);
        response.put("currentPage", 0);
        response.put("totalItems", 0);
        response.put("totalPages", 1);

        when(service.
                getProductsFiltered(0, 9, null, 100000.0, 0.0, null, null)).
                thenReturn(response);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .when()
                .get("/product/getAll")
                .then()
                .statusCode(200)
                .body("products.size()", equalTo(0))
                .body("currentPage", equalTo(0))
                .body("totalItems", equalTo(0))
                .body("totalPages", equalTo(1));


        verify(service, times(1)).
                getProductsFiltered(0, 9, null, 100000.0, 0.0, null, null);
    }
}
