package ua.tqs.humberpecas.contoller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ua.tqs.humberpecas.model.Product;
import ua.tqs.humberpecas.service.HumberService;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@WebMvcTest(HumberController.class)
class HumberControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private HumberService service;

    private ObjectMapper objectMapper;


    @BeforeEach
    void setUp() throws IOException {
        RestAssuredMockMvc.mockMvc(mvc);
        //objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }

    @Test
    public void whenGetAllProducts_thenReturnAllProducts(){

        List<Product> catalog = 
        when(service.getCatolog()).thenReturn()

    }

}