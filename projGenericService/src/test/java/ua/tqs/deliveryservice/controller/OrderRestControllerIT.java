package ua.tqs.deliveryservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.repository.PurchaseRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class OrderRestControllerIT {
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

    // ----------------------------------------------
    // --               review tests               --
    // ----------------------------------------------
    @Test
    public void testReviewWhenInvalidMin_thenBadRequest() {
        String patch = "{\"review\":\"-1\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(patch, headers);

        ResponseEntity<String> response = testRestTemplate.exchange("/order/5/review", HttpMethod.PATCH, entity, String.class);
    }

    @Test
    public void testReviewWhenInvalidMax_thenBadRequest() {
        ResponseEntity<HttpStatus> response = testRestTemplate.exchange(getBaseUrl() + "/review?order=5&review_value=6", HttpMethod.PUT, null, HttpStatus.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testReviewWhenInvalidOrderId_thenNotFound() {
        ResponseEntity<HttpStatus> response = testRestTemplate.exchange(getBaseUrl() + "/review?order=-1&review_value=3", HttpMethod.PUT, null, HttpStatus.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testValidOrderIdValidReviewValue_thenCodeOK() {
        ResponseEntity<HttpStatus> response = testRestTemplate.exchange(getBaseUrl() + "/review?order=5&review_value=5", HttpMethod.PUT, null, HttpStatus.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
    }

    public String getBaseUrl() { return "http://localhost:" + randomServerPort + "/order"; }

}
