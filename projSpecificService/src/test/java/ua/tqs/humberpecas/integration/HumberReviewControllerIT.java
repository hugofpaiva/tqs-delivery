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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.client.ExpectedCount;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.exception.UnreachableServiceException;
import ua.tqs.humberpecas.model.*;
import ua.tqs.humberpecas.repository.AddressRepository;
import ua.tqs.humberpecas.repository.PersonRepository;
import ua.tqs.humberpecas.repository.ProductRepository;
import ua.tqs.humberpecas.repository.PurchaseRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HumberReviewControllerIT {

    @LocalServerPort
    int randomServerPort;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PasswordEncoder bcryptEncoder;

    @Autowired
    private TestRestTemplate testRestTemplate;

    private Person person;
    private String token;
    private List<Product> productList;
    private Purchase purchase;
    private Review review;


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

        Address address = addressRepository.saveAndFlush(new Address("Aveiro", "3730-123","Aveiro","Portugal", person));

        this.productList = productRepository.saveAllAndFlush(Arrays.asList(
                new Product("hammer", 10.50, Category.SCREWDRIVER , "the best hammer", 3),
                new Product("hammer v2", 20.50, Category.SCREWDRIVER , "the best hammer 2.0", 4)));

        this.purchase = purchaseRepository.saveAndFlush(new Purchase(person, address, productList));

        review = new Review(12, 5);
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


//    @Test
//    @DisplayName("Add review to Rider")
//    void whenValidReview_sendToDeliveryApp() {
//
//        RestAssured.given()
//                .header("Authorization", "Bearer " + this.token)
//                .contentType("application/json")
//                .body(review)
//                .when()
//                .post(getBaseUrl() + "/add")
//                .then()
//                .statusCode(200);
//
//    }
//
//    @Test
//    @DisplayName("Add review of invalid order throws ResourseNotFound")
//    void whenInvalidOrder_thenThrowsStatusResourseNotFound(){
//
//        review.setOrderId(0);
//
//        RestAssured.given()
//                .header("Authorization", "Bearer " + this.token)
//                .contentType("application/json")
//                .body(review)
//                .when()
//                .post(getBaseUrl() + "/add")
//                .then()
//                .statusCode(404);
//
//    }



    public String getBaseUrl() {
        return "http://localhost:" + randomServerPort + "/review";
    }
}