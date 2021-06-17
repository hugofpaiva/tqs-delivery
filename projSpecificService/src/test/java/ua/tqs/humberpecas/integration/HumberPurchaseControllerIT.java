package ua.tqs.humberpecas.integration;

import io.restassured.RestAssured;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.checkerframework.checker.units.qual.A;
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
import ua.tqs.humberpecas.dto.PurchaseDTO;
import ua.tqs.humberpecas.dto.PurchaseDeliveryDTO;
import ua.tqs.humberpecas.exception.AccessNotAllowedException;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.*;
import ua.tqs.humberpecas.repository.AddressRepository;
import ua.tqs.humberpecas.repository.PersonRepository;
import ua.tqs.humberpecas.repository.ProductRepository;
import ua.tqs.humberpecas.repository.PurchaseRepository;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HumberPurchaseControllerIT {

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

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    private String token;
    private Person person;
    private Address address;
    private List<Product> catalog;
    private List<Long> productList;
    private PurchaseDTO purchaseDTO;


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


    @AfterEach
    public void resetDb() {
        purchaseRepository.deleteAll();
        purchaseRepository.flush();

        productRepository.deleteAll();
        productRepository.flush();

        addressRepository.deleteAll();
        addressRepository.flush();

        personRepository.deleteAll();
        personRepository.flush();
    }


    @BeforeEach
    public void setUp(){

        this.person = personRepository.saveAndFlush(new Person("Fernando", bcryptEncoder.encode("12345678"),"fernando@ua.pt"));

        JwtRequest request = new JwtRequest(this.person.getEmail(), "12345678");
        ResponseEntity<Map> response = testRestTemplate.postForEntity("http://localhost:" + randomServerPort + "/login", request, Map.class);
        this.token = response.getBody().get("token").toString();

        this.address = addressRepository.saveAndFlush(new Address("Aveiro", "3730-123","Aveiro","Portugal", person));

        this.catalog = productRepository.saveAllAndFlush(Arrays.asList(
                new Product(10.50, "hammer","the best hammer", Category.SCREWDRIVER ),
                new Product(20.50, "hammer v2", "the best hammer 2.0", Category.SCREWDRIVER )));

        this.productList = Arrays.asList(this.catalog.get(0).getId());
        this.purchaseDTO = new PurchaseDTO(new Date() ,this.address.getId(), productList);

    }

    @Test
    @DisplayName("Make Purchage")
    void whenValidPurchage_thenReturnOk(){

        RestAssured.given()
                .header("authorization", "Bearer " + this.token)
                .contentType("application/json")
                .body(purchaseDTO)
                .when()
                .post(getBaseUrl() + "/new")
                .then()
                .statusCode(200);

        List<Purchase> purchases = purchaseRepository.findAll();

        assertThat(purchases).hasSize(1).extracting(Purchase::getServiceOrderId).isNotNull();
        assertThat(purchases).extracting(Purchase::getPerson).containsOnly(person);


    }


    @Test
    @DisplayName("Make Purchase with invalid token throws HTTP Unauthorized")
    void whenPurchaseWithInvalidToken_thenThrowsStatus401() throws AccessNotAllowedException {

        RestAssured.given()
                .contentType("application/json")
                .body(purchaseDTO)
                .when()
                .post(getBaseUrl() + "/new")
                .then()
                .statusCode(401);


    }


    @Test
    @DisplayName("Make Purchase with Invalid Data throws HTTP status ResourseNotFound ")
    void whenPurchaseWithInvalidData_thenthenThrowsStatus404(){

        this.purchaseDTO.setAddressId(0);

        RestAssured.given()
                .contentType("application/json")
                .header("authorization", "Bearer " + this.token)
                .body(purchaseDTO)
                .when()
                .post(getBaseUrl() + "/new")
                .then()
                .statusCode(404);

    }


    public String getBaseUrl() {
        return "http://localhost:" + randomServerPort + "/purchase";
    }


}
