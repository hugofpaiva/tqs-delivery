package ua.tqs.humberpecas.integration;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ua.tqs.humberpecas.dto.AddressDTO;
import ua.tqs.humberpecas.dto.PersonDTO;
import ua.tqs.humberpecas.model.Address;
import ua.tqs.humberpecas.model.JwtRequest;
import ua.tqs.humberpecas.model.Person;
import ua.tqs.humberpecas.repository.PersonRepository;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HumberPersonControllerTemplateIT {

    @LocalServerPort
    int randomServerPort;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PasswordEncoder bcryptEncoder;

    @Autowired
    private TestRestTemplate testRestTemplate;

    private String token;
    private Person person;
    private PersonDTO personDTO;

    @Container
    public static PostgreSQLContainer container = new PostgreSQLContainer("postgres:11.12")
            .withUsername("demo")
            .withPassword("demopw")
            .withDatabaseName("specific");



    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.password", container::getPassword);
        registry.add("spring.datasource.username", container::getUsername);
    }

    @BeforeEach
    public void setUp(){
        this.person = personRepository.saveAndFlush(new Person("Fernando", bcryptEncoder.encode("12345678"),"fernando@ua.pt"));
        this.personDTO = new PersonDTO("Fernando", "12345678", "fernando@ua.pt");
    }

    @AfterEach
    public void resetDb() {
        personRepository.deleteAll();
        personRepository.flush();
    }

    @Test
    @DisplayName("Register person: email already in use")
    public void testRegisterPerson_whenEmailAlreadyInUse_thenCONFLICT_409() {
        RestAssured.given()
                .contentType("application/json")
                .body(personDTO)
                .when()
                .post(getBaseUrl() + "/register")
                .then()
                .statusCode(409);
    }

    @Test
    @DisplayName("Register person: valid personDTO and return")
    public void testRegisterPerson_whenValidPersonDTO_then200() {
        PersonDTO newUser = new PersonDTO("Melhores Devs", "da_cadeira_de_TQS", "2020@2021.UA");

        RestAssured.given()
                .contentType("application/json")
                .body(newUser)
                .when()
                .post(getBaseUrl() + "/register")
                .then()
                .body("name", equalTo(newUser.getName()))
                .body("email", equalTo(newUser.getEmail()))
                .statusCode(201);
    }

    @Test
    @DisplayName("Register person: invalid personDTO - short password")
    public void testRegisterPerson_whenInvalidPersonDTO_thenBAD_REQUEST() {
        PersonDTO newUser = new PersonDTO("Melhores Devs", "short", "2020@2021.UA");

        RestAssured.given()
                .contentType("application/json")
                .body(newUser)
                .when()
                .post(getBaseUrl() + "/register")
                .then()
                .statusCode(400);
    }

    public String getBaseUrl() {
        return "http://localhost:" + randomServerPort + "/person";
    }

}
