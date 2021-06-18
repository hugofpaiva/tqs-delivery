package ua.tqs.humberpecas.integration;

import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import ua.tqs.humberpecas.dto.AddressDTO;
import ua.tqs.humberpecas.model.Address;
import ua.tqs.humberpecas.model.JwtRequest;
import ua.tqs.humberpecas.model.Person;
import ua.tqs.humberpecas.repository.AddressRepository;
import ua.tqs.humberpecas.repository.PersonRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HumberAddressControllerTemplateIT {

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


    private String token;
    private Person person;
    private Address address;
    private AddressDTO addressDTO;


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

        this.address = addressRepository.saveAndFlush(new Address("Aveiro", "3730-123","Aveiro","Portugal", person));

        this.person.setAddresses(Set.of(this.address));
        personRepository.saveAndFlush(this.person);

        addressDTO = new AddressDTO("Aveiro", "3730-123","Aveiro","Portugal");
    }

    @AfterEach
    public void resetDb() {
        addressRepository.deleteAll();
        addressRepository.flush();

        personRepository.deleteAll();
        personRepository.flush();
    }

    // -------------------------------------
    // --      DELETE ADDRESS TESTS       --
    // -------------------------------------

    @Test
    @DisplayName("Delete Address: Invalid Token returns UNAUTHORIZED")
    public void testDeleteAddressInvalidToken_thenUnauthorized() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + "bad_token");
        ResponseEntity<String> response = testRestTemplate.exchange(
                getBaseUrl() + "/del?addressId=" + address.getId(), HttpMethod.DELETE, new HttpEntity<Object>(headers),
                String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    @DisplayName("Delete Address: Invalid AddressId returns NOT_FOUND")
    public void testDeleteAddressInvalidAddressId_thenUnauthorized() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<String> response = testRestTemplate.exchange(
                getBaseUrl() + "/del?addressId=" + -1L, HttpMethod.DELETE, new HttpEntity<Object>(headers),
                String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("Delete Address: Address doesn't belong to person returns NOT_FOUND")
    public void testDeleteAddressMismatchedPersonAndAddress_thenUnauthorized() {
        Person other_person = personRepository.saveAndFlush(new Person("Joao", bcryptEncoder.encode("nicepassword"),"joaozinho@ua.pt"));
        Address other_address = addressRepository.saveAndFlush(new Address("Aveiro", "3800-123","Aveiro","Portugal", other_person));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<String> response = testRestTemplate.exchange(
                getBaseUrl() + "/del?addressId=" + other_address.getId(), HttpMethod.DELETE, new HttpEntity<Object>(headers),
                String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("Delete Address: everything OK then return address")
    public void testDeleteAddress_thenUnauthorized() {
        HttpHeaders headers = new HttpHeaders();
        System.out.println(person.getAddresses());
        System.out.println(address);
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Address> response = testRestTemplate.exchange(
                getBaseUrl() + "/del?addressId=" + address.getId(), HttpMethod.DELETE, new HttpEntity<Object>(headers),
                Address.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody().getPostalCode() , equalTo(address.getPostalCode()));
        assertThat(response.getBody().getAddress() , equalTo(address.getAddress()));
        assertThat(response.getBody().getCountry() , equalTo(address.getCountry()));
        assertThat(response.getBody().getCity() , equalTo(address.getCity()));
        assertThat(response.getBody().isDeleted() , equalTo(true));
    }

    // -------------------------------------
    // --       ADD ADDRESS TESTS         --
    // -------------------------------------

    @Test
    @DisplayName("Add Address: Invalid Token returns UNAUTHORIZED")
    public void testAddAddressInvalidToken_thenUnauthorized() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + "bad_token");

        ResponseEntity<Address> response = testRestTemplate.exchange(
                getBaseUrl() + "/add", HttpMethod.POST, new HttpEntity<>(addressDTO, headers),
                Address.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    @DisplayName("Add Address: All valid, return new address")
    public void testAddAddressValidAddress_then200() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + this.token);

        ResponseEntity<Address> response = testRestTemplate.exchange(
                getBaseUrl() + "/add", HttpMethod.POST, new HttpEntity<>(addressDTO, headers),
                Address.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody().getAddress(), equalTo(addressDTO.getAddress()));
        assertThat(response.getBody().getCity(), equalTo(addressDTO.getCity()));
        assertThat(response.getBody().getCountry(), equalTo(addressDTO.getCountry()));
        assertThat(response.getBody().getPostalCode(), equalTo(addressDTO.getPostalCode()));
        assertThat(response.getBody().isDeleted(), equalTo(false));

    }

    // -------------------------------------
    // --       GET ADDRESS TESTS         --
    // -------------------------------------

    @Test
    @DisplayName("Get Person Addresses: Invalid Token, return UNAUTHORIZED")
    public void testGetAddressesInvalidToken_thenUnauthorized() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + "bad_token");
        ResponseEntity<List> response = testRestTemplate.exchange(
                getBaseUrl(), HttpMethod.GET, new HttpEntity<Object>(headers),
                List.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    @DisplayName("Get Person Addresses: Invalid Token, return UNAUTHORIZED")
    public void testGetAddressesEverythingValid_thenOk() {
        HttpHeaders headers = new HttpHeaders();
        ObjectMapper mapper = new ObjectMapper();

        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<List> response = testRestTemplate.exchange(
                getBaseUrl() + "/getAll", HttpMethod.GET, new HttpEntity<Object>(headers),
                List.class);

        List<Address> addresses = mapper.convertValue(
                response.getBody(),
                new TypeReference<List<Address>>() {
                }
        );

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(addresses.get(0).getPostalCode() , equalTo(address.getPostalCode()));
        assertThat(addresses.get(0).getAddress() , equalTo(address.getAddress()));
        assertThat(addresses.get(0).getCountry() , equalTo(address.getCountry()));
        assertThat(addresses.get(0).getCity() , equalTo(address.getCity()));
        assertThat(addresses.get(0).isDeleted() , equalTo(false));

    }

    public String getBaseUrl() {
        return "http://localhost:" + randomServerPort + "/address";
    }


}
