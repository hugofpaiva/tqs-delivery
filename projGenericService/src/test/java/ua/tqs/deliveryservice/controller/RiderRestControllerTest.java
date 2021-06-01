package ua.tqs.deliveryservice.controller;

import jdk.jfr.consumer.RecordedStackTrace;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.okhttp3.Response;
import ua.tqs.deliveryservice.model.Address;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.model.Store;
import ua.tqs.deliveryservice.repository.AddressRepository;
import ua.tqs.deliveryservice.repository.PurchaseRepository;
import ua.tqs.deliveryservice.repository.RiderRepository;
import ua.tqs.deliveryservice.repository.StoreRepository;

import static org.junit.jupiter.api.Assertions.*;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class RiderRestControllerTest {
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
    private TestRestTemplate testRestTemplate;

    @Autowired
    private RiderRepository riderRepository;

    // ----------------------------------------------
    // --               status tests               --
    // ----------------------------------------------
    

    // ----------------------------------------------
    // --               review tests               --
    // ----------------------------------------------
    @Test
    public void testReviewWhenInvalidMin_thenBadRequest() {
        ResponseEntity<HttpStatus> response = testRestTemplate.getForEntity(getBaseUrl() + "/review?order=5&review_value=-1", HttpStatus.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testReviewWhenInvalidMax_thenBadRequest() {
        ResponseEntity<HttpStatus> response = testRestTemplate.getForEntity(getBaseUrl() + "/review?order=5&review_value=6", HttpStatus.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testReviewWhenInvalidOrderId_thenNotFound() {
        ResponseEntity<HttpStatus> response = testRestTemplate.getForEntity(getBaseUrl() + "/review?order=-1&review_value=3", HttpStatus.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testValidOrderIdValidReviewValue_thenCodeOK() {
        ResponseEntity<HttpStatus> response = testRestTemplate.getForEntity(getBaseUrl() + "/review?order=5&review_value=5", HttpStatus.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
    }

    public String getBaseUrl() {
        return "http://localhost:" + randomServerPort + "/rider";
    }
}