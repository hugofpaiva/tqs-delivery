package ua.tqs.deliveryservice.integration;

import org.junit.jupiter.api.AfterEach;
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
import ua.tqs.deliveryservice.model.JwtRequest;
import ua.tqs.deliveryservice.model.Manager;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.repository.PersonRepository;
import ua.tqs.deliveryservice.repository.RiderRepository;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AuthControllerTemplateIT  {

    @Container
    public static PostgreSQLContainer container = new PostgreSQLContainer("postgres:11.12")
            .withUsername("demo")
            .withPassword("demopw")
            .withDatabaseName("shop");


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
    private PersonRepository personRepository;

    @Autowired
    private RiderRepository riderRepository;

    @Autowired
    private PasswordEncoder bcryptEncoder;

    // ----------------------------------
    // --         LOGIN TESTS          --
    // ----------------------------------

    @AfterEach
    public void cleanUp() {
        riderRepository.deleteAll();
        riderRepository.flush();
    }

    @Test
    public void testLoginWhenInvalidEmail_thenUnauthorized() {
        JwtRequest request = new JwtRequest("email@asd.com", "aswdd");
        ResponseEntity<String> response =
                testRestTemplate.postForEntity(getBaseUrl()+"/login", request, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void testLoginWhenInvalidPwRider_thenUnauthorized() {
        Rider rider = new Rider();
        rider.setEmail("mail@example.com");
        rider.setPwd(bcryptEncoder.encode("aRightPassword"));
        rider.setName("Joao");
        personRepository.saveAndFlush(rider);

        JwtRequest request = new JwtRequest("mail@example.com", "aswdd");
        ResponseEntity<String> response = testRestTemplate.postForEntity(getBaseUrl()+"/login", request, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
        personRepository.delete(rider);
        personRepository.flush();
    }

    @Test
    public void testLoginWhenInvalidPwManager_thenUnauthorized() {
        Manager manager = new Manager();
        manager.setEmail("mail@example.com");
        manager.setPwd(bcryptEncoder.encode("aRightPassword"));
        manager.setName("Joao");
        personRepository.saveAndFlush(manager);

        JwtRequest request = new JwtRequest("mail@example.com", "aswdd");
        ResponseEntity<String> response = testRestTemplate.postForEntity(getBaseUrl()+"/login", request, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
        personRepository.delete(manager);
        personRepository.flush();
    }

    @Test
    public void testLoginWhenValidDataRider_thenAuthorized() {
        Rider rider = new Rider();
        rider.setEmail("mail@example.com");
        rider.setPwd(bcryptEncoder.encode("aRightPassword"));
        rider.setName("Joao");
        personRepository.saveAndFlush(rider);

        JwtRequest request = new JwtRequest("mail@example.com", "aRightPassword");
        ResponseEntity<Map> response =
                testRestTemplate.postForEntity(getBaseUrl()+"/login", request, Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        assertEquals(((Map<String, String>) response.getBody().get("type")).get("authority"), rider.getClass().getSimpleName());
        assertEquals(response.getBody().get("name"), rider.getName());
        personRepository.delete(rider);
        personRepository.flush();
    }

    @Test
    public void testLoginWhenValidDataManager_thenAuthorized() {
        Manager manager = new Manager();
        manager.setEmail("mail@example.com");
        manager.setPwd(bcryptEncoder.encode("aRightPassword"));
        manager.setName("Joao");
        personRepository.saveAndFlush(manager);

        JwtRequest request = new JwtRequest("mail@example.com", "aRightPassword");
        ResponseEntity<Map> response =
                testRestTemplate.postForEntity(getBaseUrl()+"/login", request, Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        assertEquals(((Map<String, String>) response.getBody().get("type")).get("authority"), manager.getClass().getSimpleName());
        assertEquals(response.getBody().get("name"), manager.getName());
        personRepository.delete(manager);
        personRepository.flush();
    }

    // ----------------------------------
    // --     REGISTER RIDER TESTS     --
    // ----------------------------------

    @Test
    public void testInvalidEmail_thenBadRequest() {
        Map<String, String> data = new HashMap<>();
        data.put("email", null);
        data.put("name", "A Nice Name");
        data.put("pwd", "And_A_str0ngPasswor2drd");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(data, headers);

        ResponseEntity<Map> response = testRestTemplate.postForEntity(getBaseUrl() + "/register", entity, Map.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testInvalidPwd_thenBadRequest() {
        Map<String, String> data = new HashMap<>();
        data.put("email", "example@tqs.ua");
        data.put("name", "A Nice Name");
        data.put("pwd", "weak");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(data, headers);

        ResponseEntity<Map> response = testRestTemplate.postForEntity(getBaseUrl() + "/register", entity, Map.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testInvalidName_thenBadRequest() {
        Map<String, String> data = new HashMap<>();
        data.put("email", "example@tqs.ua");
        data.put("name", null);
        data.put("pwd", "strongggg");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(data, headers);

        ResponseEntity<Map> response = testRestTemplate.postForEntity(getBaseUrl() + "/register", entity, Map.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testEverythingValid_thenCreated() {
        Map<String, String> data = new HashMap<>();
        data.put("email", "example@tqs.ua");
        data.put("name", "A very nice name");
        data.put("pwd", "strongggg");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(data, headers);

        ResponseEntity<Rider> response = testRestTemplate.postForEntity(getBaseUrl() + "/register", entity, Rider.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.CREATED));
        assertThat(response.getBody().getEmail(), equalTo(data.get("email")));
        assertThat(response.getBody().getName(), equalTo(data.get("name")));
    }

    @Test
    public void testRegister_whenEmailAlreadyInUse_thenCONFLICT() {
        Map<String, String> data = new HashMap<>();
        data.put("email", "example@tqs.ua");
        data.put("name", "A very nice name");
        data.put("pwd", "strongggg");

        Rider existingRider = new Rider(data.get("name"), data.get("pwd"), data.get("email"));
        riderRepository.saveAndFlush(existingRider);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(data, headers);

        ResponseEntity<Rider> response = testRestTemplate.postForEntity(getBaseUrl() + "/register", entity, Rider.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.CONFLICT));
    }

    public String getBaseUrl() {
        return "http://localhost:"+randomServerPort;
    }

}
