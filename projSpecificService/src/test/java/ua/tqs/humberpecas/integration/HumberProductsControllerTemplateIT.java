package ua.tqs.humberpecas.integration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import ua.tqs.humberpecas.model.*;
import ua.tqs.humberpecas.repository.AddressRepository;
import ua.tqs.humberpecas.repository.PersonRepository;
import ua.tqs.humberpecas.repository.ProductRepository;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class HumberProductsControllerTemplateIT {
    private Person person;
    private Address personAddr;
    private String token;
    private Product prod1;
    private Product prod2;

    @Container
    public static PostgreSQLContainer container = new PostgreSQLContainer("postgres:11.12")
            .withUsername("demo")
            .withPassword("demopw")
            .withDatabaseName("delivery");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.password", container::getPassword);
        registry.add("spring.datasource.username", container::getUsername);
    }

    @LocalServerPort
    int randomServerPort;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PasswordEncoder bcryptEncoder;

    @Autowired
    private TestRestTemplate testRestTemplate;

    public String getBaseUrl() {
        return "http://localhost:" + randomServerPort + "/product/";
    }

    @BeforeEach
    public void setUp() {
        this.person = new Person("Maria Joana", bcryptEncoder.encode("aRightPassword"), "maria2000@gmail.com");
        this.personAddr = new Address("Universidade de Aveiro", "3800-000", "Aveiro", "Portugal");
        this.person.setAddresses(Set.of(this.personAddr));

        this.prod1 = new Product("Prego Grande", 0.35, Category.NAILS, "10/10 recomendo", "randomFakeImageNail.png");
        this.prod2 = new Product("Alicate para Dentes", 4.21, Category.PLIERS, "Funciona", "nonExistent.jpeg");

        addressRepository.saveAndFlush(this.personAddr);
        personRepository.saveAndFlush(this.person);
        productRepository.saveAllAndFlush(Arrays.asList(this.prod1, this.prod2));

        JwtRequest request = new JwtRequest(this.person.getEmail(), "aRightPassword");
        ResponseEntity<Map> response = testRestTemplate.postForEntity("http://localhost:" + randomServerPort + "/login", request, Map.class);
        this.token = response.getBody().get("token").toString();
    }

    @AfterEach
    public void cleanUp() {
        personRepository.deleteAll();
        personRepository.flush();

        productRepository.deleteAll();
        productRepository.flush();

        addressRepository.deleteAll();
        addressRepository.flush();
    }

    // -------------------------------------
    // --   GET FILTERED PRODUCTS TESTS   --
    // -------------------------------------


    @Test
    public void testGetProductsWhenInvalidPageNo_thenBadRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "getAll?pageNo=" + -1, HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testGetProductsWhenInvalidPageSize_thenBadRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "getAll?pageSize=" + 0, HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testWhenInvalidMinPrice_thenBadRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "getAll?minPrice=" + -3, HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testWhenInvalidMaxPrice_thenBadRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "getAll?maxPrice=" + -2, HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testWhenInvalidMinPriceHigherThanMaxPrice_thenBadRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "getAll?maxPrice=" + 5 + "&minPrice=" + 10, HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testWhenAllValid_filterWithCategory_thenOk() {
        HttpHeaders headers = new HttpHeaders();
        ObjectMapper mapper = new ObjectMapper();

        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl()
                        + "getAll?maxPrice=" + 10
                        + "&minPrice=" + 0
                        + "&category=" + Category.NAILS
                , HttpMethod.GET, new HttpEntity<Object>(headers), Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();

        List<Product> products = mapper.convertValue(
            found.get("products"), new TypeReference<List<Product>>() { }
        );

        Assertions.assertThat(products).contains(prod1);
        Assertions.assertThat(found.get("currentPage")).isEqualTo(0);
        Assertions.assertThat(found.get("totalItems")).isEqualTo(1);
        Assertions.assertThat(found.get("totalPages")).isEqualTo(1);
    }

    @Test
    public void testWhenAllValid_filterWithCategoryAndName_thenOk() {
        HttpHeaders headers = new HttpHeaders();
        ObjectMapper mapper = new ObjectMapper();

        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl()
                        + "getAll?maxPrice=" + 10
                        + "&minPrice=" + 0
                        + "&category=" + Category.PLIERS
                        + "&name=" + this.prod2.getName()
                , HttpMethod.GET, new HttpEntity<Object>(headers), Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();

        List<Product> products = mapper.convertValue(
                found.get("products"), new TypeReference<List<Product>>() { }
        );

        Assertions.assertThat(products).contains(prod2);
        Assertions.assertThat(found.get("currentPage")).isEqualTo(0);
        Assertions.assertThat(found.get("totalItems")).isEqualTo(1);
        Assertions.assertThat(found.get("totalPages")).isEqualTo(1);
    }

    @Test
    public void testWhenAllValid_filterWithName_thenOk() {
        HttpHeaders headers = new HttpHeaders();
        ObjectMapper mapper = new ObjectMapper();

        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl()
                        + "getAll?maxPrice=" + 10
                        + "&minPrice=" + 0
                        + "&name=" + this.prod2.getName()
                , HttpMethod.GET, new HttpEntity<Object>(headers), Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();

        List<Product> products = mapper.convertValue(
                found.get("products"), new TypeReference<List<Product>>() { }
        );

        Assertions.assertThat(products).contains(prod2);
        Assertions.assertThat(found.get("currentPage")).isEqualTo(0);
        Assertions.assertThat(found.get("totalItems")).isEqualTo(1);
        Assertions.assertThat(found.get("totalPages")).isEqualTo(1);
    }

    @Test
    public void testWhenAllValid_noFilters_thenOk() {
        HttpHeaders headers = new HttpHeaders();
        ObjectMapper mapper = new ObjectMapper();

        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "getAll" , HttpMethod.GET, new HttpEntity<Object>(headers), Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();

        List<Product> products = mapper.convertValue(
                found.get("products"), new TypeReference<List<Product>>() { }
        );

        Assertions.assertThat(products).contains(prod1);
        Assertions.assertThat(products).contains(prod2);
        Assertions.assertThat(found.get("currentPage")).isEqualTo(0);
        Assertions.assertThat(found.get("totalItems")).isEqualTo(2);
        Assertions.assertThat(found.get("totalPages")).isEqualTo(1);
    }

    @Test
    public void testWhenAllValid_orderByPrice_thenOk() {
        HttpHeaders headers = new HttpHeaders();
        ObjectMapper mapper = new ObjectMapper();

        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "getAll?orderBy=price"  , HttpMethod.GET, new HttpEntity<Object>(headers), Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();

        List<Product> products = mapper.convertValue(
                found.get("products"), new TypeReference<List<Product>>() { }
        );

        Assertions.assertThat(products).element(0).isEqualTo(prod1);
        Assertions.assertThat(products).element(1).isEqualTo(prod2);

        Assertions.assertThat(found.get("currentPage")).isEqualTo(0);
        Assertions.assertThat(found.get("totalItems")).isEqualTo(2);
        Assertions.assertThat(found.get("totalPages")).isEqualTo(1);
    }
}
