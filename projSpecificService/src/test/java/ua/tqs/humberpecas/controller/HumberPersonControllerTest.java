package ua.tqs.humberpecas.controller;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;
import ua.tqs.humberpecas.configuration.JwtRequestFilter;
import ua.tqs.humberpecas.configuration.WebSecurityConfig;
import ua.tqs.humberpecas.dto.AddressDTO;
import ua.tqs.humberpecas.dto.PersonDTO;
import ua.tqs.humberpecas.exception.DuplicatedObjectException;
import ua.tqs.humberpecas.model.Person;
import ua.tqs.humberpecas.services.HumberPersonService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

// TODO: Refactor
@WebMvcTest(value = HumberPersonController.class, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = WebSecurityConfig.class)})
@AutoConfigureMockMvc(addFilters = false)
class HumberPersonControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private HumberPersonService service;

    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    @BeforeEach
    void setUp() throws IOException {
        RestAssuredMockMvc.mockMvc(mvc);

    }

    @Test
    @DisplayName("User registration")
    void whenValidRegister_thenReturnCrated() throws DuplicatedObjectException {

        List<AddressDTO> addresses = Arrays.asList(new AddressDTO("Aveiro", "3730-123","Aveiro","Portugal",1));

        PersonDTO p = new PersonDTO("Fernando", "12345678","fernando@ua.pt", addresses);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(p)
                .when()
                .post("/person/register")
                .then()
                .statusCode(201);

        verify(service, times(1)).register(p);

    }


    @ParameterizedTest
    @MethodSource("invalidAccounts")
    @DisplayName("When User inserts invalids accounts's parameter returns HTTP status Bad Request")
    void whenInvalidRegister_thenReturnStatus400(String name, String pwd,String email) throws DuplicatedObjectException {

        List<AddressDTO> addresses = Arrays.asList(new AddressDTO("Aveiro", "3730-123","Aveiro","Portugal",1));

        PersonDTO p = new PersonDTO(name, pwd, email, addresses);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(new Person(name, pwd, email))
                .when()
                .post("/person/register")
                .then()
                .statusCode(400);

        verify(service, times(0)).register(p);


    }

    @Test
    @DisplayName("When account already exists returns HTTP status Conflict")
    void whenUserAlreadyExists_thenReturnStatus409() throws DuplicatedObjectException {

        List<AddressDTO> addresses = Arrays.asList(new AddressDTO("Aveiro", "3730-123","Aveiro","Portugal",1));

        PersonDTO p = new PersonDTO("Fernando", "12345678","fernando@ua.pt", addresses);
        doThrow(new DuplicatedObjectException("User alerady exists!")).when(service).register(p);
        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(p)
                .when()
                .post("/person/register")
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
