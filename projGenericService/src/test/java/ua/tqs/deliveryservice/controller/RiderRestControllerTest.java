package ua.tqs.deliveryservice.controller;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ua.tqs.deliveryservice.model.*;
import ua.tqs.deliveryservice.repository.RiderRepository;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;

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

    HttpHeaders headers = new HttpHeaders();

    // ----------------------------------------------
    // --               status tests               --
    // ----------------------------------------------
    @Test
    public void testOrderStatusWhenInvalidId_thenBadRequest() {
        HttpEntity<Map> entity = new HttpEntity<>(new HashMap<>(), headers);
        ResponseEntity<HttpStatus> response = testRestTemplate.exchange(getBaseUrl() + "/order/-1/status", HttpMethod.PUT, entity,  HttpStatus.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testOrderStatusEverythingValid_thenOK() {
        Map<String, Object> data = new HashMap<>();
        data.put("order_id", 5);
        data.put("status", Status.ACCEPTED.toString());
        JSONObject json = new JSONObject(data);

        HttpEntity<Map> entity = new HttpEntity<>(data, headers);

        ResponseEntity<String> response = testRestTemplate.exchange(getBaseUrl() + "/order/5/status", HttpMethod.PUT, entity,  String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(json.toString()));
    }

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