package ua.tqs.humberpecas.integration;

import io.restassured.RestAssured;
import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import ua.tqs.humberpecas.dto.PurchaseDTO;
import ua.tqs.humberpecas.exception.AccessNotAllowedException;
import ua.tqs.humberpecas.model.*;
import ua.tqs.humberpecas.repository.AddressRepository;
import ua.tqs.humberpecas.repository.PersonRepository;
import ua.tqs.humberpecas.repository.ProductRepository;
import ua.tqs.humberpecas.repository.PurchaseRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HumberPurchaseControllerTemplateIT {

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
                new Product("hammer", 10.50, Category.SCREWDRIVER , "the best hammer", "image_url"),
                new Product("hammer v2", 20.50, Category.SCREWDRIVER , "the best hammer 2.0", "image_url")));

        this.productList = Arrays.asList(this.catalog.get(0).getId());
        this.purchaseDTO = new PurchaseDTO(this.address.getId(), productList);

    }

    /* ******************************************
    *               MAKE NEW PURCHASE           *
    * *******************************************
    */
    @Test
    @DisplayName("Make Purchase")
    void whenValidPurchase_thenReturnOk(){

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
    void whenPurchaseWithInvalidData_thenThrowsStatus404(){

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

    /* *******************************************
     *              GET ALL PURCHASES            *
     * *******************************************
     */


    @Test
    @DisplayName("Get all user purchases when pageNo invalid")
    void testGetAllUserPurchases_whenInvalidPageNo_then400(){

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "/getAll?pageNo=" + -1, HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        MatcherAssert.assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));

    }

    @Test
    @DisplayName("Get all user purchases when pageSize invalid")
    void testGetAllUserPurchases_whenInvalidPageSize_then400(){

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "/getAll?pageSize=" + 0, HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        MatcherAssert.assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("Get all user purchases when invalid token")
    void testGetAllUserPurchases_whenInvalidToken_then400(){

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer bad_token");
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "/getAll", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        MatcherAssert.assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));

    }

    @Test
    @DisplayName("Get all user purchases when everything is valid but no purchases")
    void testGetAllUserPurchases_whenEverythingValidButNoPurchases_thenOK_Return(){
        HttpHeaders headers = new HttpHeaders();
        ObjectMapper mapper = new ObjectMapper();

        headers.set("Authorization", "Bearer " + token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "/getAll", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        MatcherAssert.assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();

        List<Product> products = mapper.convertValue(
                found.get("products"), new TypeReference<List<Product>>() {
                }
        );

        Assertions.assertThat(found.get("orders")).isEqualTo(new ArrayList<>());
        Assertions.assertThat(found.get("currentPage")).isEqualTo(0);
        Assertions.assertThat(found.get("totalItems")).isEqualTo(0);
        Assertions.assertThat(found.get("totalPages")).isEqualTo(0);
        Assertions.assertThat(found.get("reviewsGiven")).isEqualTo(0);
    }

    @Test
    @DisplayName("Get all user purchases when everything is valid ")
    void testGetAllUserPurchases_whenEverythingValid_thenOK_Return(){
        Product prego = new Product("Prego Grande", 0.35, Category.NAILS, "10/10 recomendo", "randomFakeImageNail.png");
        productRepository.saveAndFlush(prego);
        purchaseRepository.saveAndFlush(new Purchase(person, address, Arrays.asList(prego)));

        HttpHeaders headers = new HttpHeaders();
        ObjectMapper mapper = new ObjectMapper();

        headers.set("Authorization", "Bearer " + token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "/getAll", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        MatcherAssert.assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        Map<String, Object> found = response.getBody();

        List<Purchase> orders = mapper.convertValue(
                found.get("orders"), new TypeReference<List<Purchase>>() {
                }
        );

        Assertions.assertThat(orders.get(0).getProducts()).contains(prego);
        Assertions.assertThat(found.get("currentPage")).isEqualTo(0);
        Assertions.assertThat(found.get("totalItems")).isEqualTo(1);
        Assertions.assertThat(found.get("totalPages")).isEqualTo(1);
        Assertions.assertThat(found.get("reviewsGiven")).isEqualTo(0);
    }


    public String getBaseUrl() {
        return "http://localhost:" + randomServerPort + "/purchase";
    }


}
