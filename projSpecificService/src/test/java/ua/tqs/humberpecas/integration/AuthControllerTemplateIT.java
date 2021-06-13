package ua.tqs.humberpecas.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ua.tqs.humberpecas.model.JwtRequest;
import ua.tqs.humberpecas.model.Person;
import ua.tqs.humberpecas.repository.PersonRepository;

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
    private PasswordEncoder bcryptEncoder;

    @Test
    public void testLoginWhenInvalidEmail_thenUnauthorized() {
        JwtRequest request = new JwtRequest("email@asd.com", "aswdd");
        ResponseEntity<String> response =
                testRestTemplate.postForEntity(getBaseUrl()+"/login", request, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void testLoginWhenInvalidPw_thenUnauthorized() {
        Person person = new Person();
        person.setEmail("joao@example.com");
        person.setPwd(bcryptEncoder.encode("aRightPassword"));
        person.setName("Joao");
        personRepository.saveAndFlush(person);

        JwtRequest request = new JwtRequest("joao@example.com", "aswdd");
        ResponseEntity<String> response = testRestTemplate.postForEntity(getBaseUrl()+"/login", request, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
        personRepository.delete(person);
        personRepository.flush();
    }

    @Test
    public void testLoginWhenValidData_thenAuthorized() {
        Person person = new Person();
        person.setEmail("joao@example.com");
        person.setPwd(bcryptEncoder.encode("aRightPassword"));
        person.setName("Joao");
        personRepository.saveAndFlush(person);

        JwtRequest request = new JwtRequest("joao@example.com", "aRightPassword");
        ResponseEntity<Map> response =
                testRestTemplate.postForEntity(getBaseUrl()+"/login", request, Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        assertEquals(((Map<String, String>) response.getBody().get("type")).get("authority"), person.getClass().getSimpleName());
        assertEquals(response.getBody().get("name"), person.getName());
        personRepository.delete(person);
        personRepository.flush();

    }

    public String getBaseUrl() {
        return "http://localhost:"+randomServerPort;
    }

}