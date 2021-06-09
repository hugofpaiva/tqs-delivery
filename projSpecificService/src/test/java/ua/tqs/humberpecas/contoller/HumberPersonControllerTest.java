package ua.tqs.humberpecas.contoller;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;
import ua.tqs.humberpecas.dto.AddressDTO;
import ua.tqs.humberpecas.dto.PersonDTO;
import ua.tqs.humberpecas.model.Person;
import ua.tqs.humberpecas.service.HumberService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;


@WebMvcTest(HumberController.class)
public class HumberPersonControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private HumberService service;

    @BeforeEach
    void setUp() throws IOException {
        RestAssuredMockMvc.mockMvc(mvc);

    }

    @Test
    @DisplayName("User registration")
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
    @DisplayName("When User inserts invalids accounts's parameter returns HTTP status Bad Request")
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
    @DisplayName("When account already exists returns HTTP status Conflict")
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

}
