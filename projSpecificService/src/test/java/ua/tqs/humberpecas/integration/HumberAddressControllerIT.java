package ua.tqs.humberpecas.integration;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ua.tqs.humberpecas.dto.AddressDTO;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.Address;
import ua.tqs.humberpecas.model.JwtRequest;
import ua.tqs.humberpecas.model.Person;
import ua.tqs.humberpecas.repository.AddressRepository;
import ua.tqs.humberpecas.repository.PersonRepository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HumberAddressControllerIT {

    @LocalServerPort
    int randomServerPort;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PasswordEncoder bcryptEncoder;

    @Autowired
    private TestRestTemplate testRestTemplate;


    private String token;
    private Person person;
    private Address address;
    private AddressDTO addressDTO;


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

        JwtRequest request = new JwtRequest(this.person.getEmail(), "12345678");
        ResponseEntity<Map> response = testRestTemplate.postForEntity("http://localhost:" + randomServerPort + "/login", request, Map.class);
        this.token = response.getBody().get("token").toString();

        this.address = addressRepository.saveAndFlush(new Address("Aveiro", "3730-123","Aveiro","Portugal", person));

        addressDTO = new AddressDTO("Coimbra", "3730-134","Coimbra","Portugal",this.person.getId());
        addressDTO.setAddressId(this.address.getId());
    }

    @AfterEach
    public void resetDb() {
        addressRepository.deleteAll();
        addressRepository.flush();

        personRepository.deleteAll();
        personRepository.flush();
    }


    @Test
    @DisplayName("Update User address")
    public void whenUpdateValidAddress_thenReturnStatusOk(){

        RestAssured.given()
                .header("Authorization", "Bearer " + this.token)
                .contentType("application/json")
                .body(addressDTO)
                .when()
                .put(getBaseUrl() + "/update")
                .then()
                .statusCode(200);

        List<Address> addresses = addressRepository.findAll();

        assertThat(addresses).hasSize(1).extracting(Address::getAddress).containsOnly("Coimbra");

    }


    @Test
    @DisplayName("Update invalid User Address returns HTTP Not Found")
    public void whenUpdateNonexistentAddress_thenReturnsStatus404() throws ResourceNotFoundException {

        addressDTO.setAddressId(5L);

        RestAssured.given()
                .header("Authorization", "Bearer " + this.token)
                .contentType("application/json")
                .body(this.addressDTO)
                .when()
                .put(getBaseUrl() + "/update")
                .then()
                .statusCode(404);

    }


    public String getBaseUrl() {
        return "http://localhost:" + randomServerPort + "/address";
    }


}
