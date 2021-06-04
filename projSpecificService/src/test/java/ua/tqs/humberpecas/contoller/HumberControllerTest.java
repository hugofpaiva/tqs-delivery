package ua.tqs.humberpecas.contoller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ua.tqs.humberpecas.service.HumberService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;


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

}