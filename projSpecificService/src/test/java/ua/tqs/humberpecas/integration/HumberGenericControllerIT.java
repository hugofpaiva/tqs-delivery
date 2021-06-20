package ua.tqs.humberpecas.integration;


import io.restassured.RestAssured;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ua.tqs.humberpecas.dto.AddressDTO;
import ua.tqs.humberpecas.dto.ServerRiderDTO;
import ua.tqs.humberpecas.dto.ServerStatusDTO;
import ua.tqs.humberpecas.exception.InvalidLoginException;
import ua.tqs.humberpecas.exception.InvalidOperationException;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.*;
import ua.tqs.humberpecas.repository.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HumberGenericControllerIT {

    @LocalServerPort
    int randomServerPort;


    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private GenericRepository genericRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestRestTemplate testRestTemplate;

    private String token;
    private ServerStatusDTO statusDTO;
    private Long serverOrderId;
    private Generic generic;
    private Purchase purchase;
    private ServerRiderDTO serverRiderDTO;



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
        this.token = "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDcwOTYwNDMsImlhdCI6MTYyMzA5OTI0MywiU3ViamVjdCI6Ikh1bWJlclBlY2FzIn0.oEZD63J134yUxHl658oSDJrw32BZcYHQbveZw8koAgP-2_d-8aH2wgJYJMlGnKIugOiI8H9Aa4OjPMWMUl9BFw";
        this.generic = genericRepository.saveAndFlush(new Generic("delivery", this.token));

        Person person = new Person("Fernando", "12345678","fernando@ua.pt");
        Address address  = new Address("Aveiro", "3730-123","Aveiro","Portugal", person);

        personRepository.saveAndFlush(person);
        addressRepository.saveAndFlush(address);

        List<Product> productList = Arrays.asList(
                new Product( 10.50 , "hammer", "the best hammer", Category.SCREWDRIVER),
                new Product(20.50 , "hammer v2", "the best hammer 2.0",  Category.SCREWDRIVER));
        productRepository.saveAllAndFlush(productList);

        Purchase p = new Purchase(person, address, productList);
        p.setServiceOrderId(5L);

        this.purchase = purchaseRepository.saveAndFlush(p);

        this.serverOrderId = purchase.getServiceOrderId();

        this.statusDTO = new ServerStatusDTO(PurchaseStatus.ACCEPTED);
        this.serverRiderDTO = new ServerRiderDTO("tone");
    }

    @AfterEach
    public void resetDb() {
        purchaseRepository.deleteAll();
        purchaseRepository.flush();

        genericRepository.deleteAll();
        genericRepository.flush();

        addressRepository.deleteAll();
        addressRepository.flush();

        personRepository.deleteAll();
        personRepository.flush();
    }


    @Test
    @DisplayName("Update Order Status")
    void whenUpdateValidOrder_thenUpdateOrder(){

        RestAssured.given()
                .contentType("application/json")
                .header("authorization", "Bearer " +this.token)
                .body(statusDTO)
                .when()
                .put(getBaseUrl() + "/updateStatus?serverOrderId=5")
                .then()
                .statusCode(200);

        List<Purchase> purchaseList = purchaseRepository.findAll();

        assertThat(purchaseList).hasSize(1).extracting(Purchase::getStatus).containsOnly(PurchaseStatus.ACCEPTED);


    }


    @Test
    @DisplayName("Update Order Status with invalid token throws Unauthorized ")
    void whenUpdateOrderInvalidToken_thenThrowsStatusUnauthorized(){

        RestAssured.given()
                .contentType("application/json")
                .header("authorization", "Bearer " +this.token.replace("e", "i"))
                .body(statusDTO)
                .when()
                .put(getBaseUrl() + "/updateStatus?serverOrderId=5")
                .then()
                .statusCode(401);


    }

    @Test
    @DisplayName("Update Status of invalid order throws ResourseNotFound")
    void whenUpdateWithOrder_thenThrowsStatusResourseNotFound(){

        RestAssured.given()
                .contentType("application/json")
                .header("authorization", "Bearer " +this.token)
                .body(statusDTO)
                .when()
                .put(getBaseUrl() + "/updateStatus?serverOrderId=1")
                .then()
                .statusCode(404);

    }

    @Test
    @DisplayName("Update Status with invalid status throws BadRequest")
    void whenUpdateWithInvalidStatus_thenThrowsStatusBadRequest(){

        Map<String, String> orderStatus = new HashMap<>();
        orderStatus.put("orderStatus", "ok");

        RestAssured.given()
                .contentType("application/json")
                .header("authorization", "Bearer " +this.token)
                .body(orderStatus)
                .when()
                .put(getBaseUrl() + "/updateStatus?serverOrderId=5")
                .then()
                .statusCode(400);

    }

    @Test
    @DisplayName("Set Rider")
    void whenSetRider_thenReturnStatusOk(){

        RestAssured.given()
                .contentType("application/json")
                .header("authorization", "Bearer " + this.token)
                .body(serverRiderDTO)
                .when()
                .put(getBaseUrl() + "/setRider?serverOrderId=" + serverOrderId)
                .then()
                .statusCode(200);

        List<Purchase> purchaseList = purchaseRepository.findAll();

        assertThat(purchaseList).hasSize(1).extracting(Purchase::getRiderName).containsOnly(serverRiderDTO.getRider());

    }


    @Test
    @DisplayName("Set Rider of invalid order throws BadRequest")
    void whenSetRiderAssignedOrder_thenThrowsStatusBadRequest(){

        Map<String, String> request = new HashMap<>();
        request.put("x", "y");

        RestAssured.given()
                .contentType("application/json")
                .header("authorization", "Bearer " + this.token)
                .body(request)
                .when()
                .put(getBaseUrl() + "/setRider?serverOrderId=" + serverOrderId)
                .then()
                .statusCode(400);


    }


    @Test
    @DisplayName("Set Rider of invalid genric token throws Unauthorized")
    void whenSetRiderInvalidToken_thenThrowsStatusUnauthorized(){


        RestAssured.given()
                .contentType("application/json")
                .header("authorization", "Bearer " + this.token.replace("e", "i"))
                .body(serverRiderDTO)
                .when()
                .put(getBaseUrl() + "/setRider?serverOrderId=" + serverOrderId)
                .then()
                .statusCode(401);

    }


    @Test
    @DisplayName("Set Rider of invalid order throws ResourseNotFound")
    void whenSetRiderWithInvalidOrder_thenThrowsStatusResourseNotFound(){

        RestAssured.given()
                .contentType("application/json")
                .header("authorization", "Bearer " + this.token)
                .body(serverRiderDTO)
                .when()
                .put(getBaseUrl() + "/setRider?serverOrderId=0")
                .then()
                .statusCode(404);

    }

    @Test
    @DisplayName("Set Rider with invalid input token throws BadRequest")
    void whenSetRiderInvalidInput_thenThrowsStatusBadRequest(){

        Map<String, String> request = new HashMap<>();
        request.put("x", "y");

        RestAssured.given()
                .contentType("application/json")
                .header("authorization", "Bearer " + this.token)
                .body(request)
                .when()
                .put(getBaseUrl() + "/setRider?serverOrderId=" + serverOrderId)
                .then()
                .statusCode(400);

    }

    @Test
    @DisplayName("Set Rider wtihout generic token throws BadRequest")
    void whenSetRiderWithoutToken_thenThrowsStatusBadRequest(){

        RestAssured.given()
                .contentType("application/json")
                .body(serverRiderDTO)
                .when()
                .put(getBaseUrl() + "/setRider?serverOrderId=" + serverOrderId)
                .then()
                .statusCode(400);


    }



    public String getBaseUrl() {
        return "http://localhost:" + randomServerPort + "/delivery";
    }



}
