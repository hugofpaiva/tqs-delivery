package ua.tqs.deliveryservice.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.AbstractPageRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ua.tqs.deliveryservice.model.Person;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.repository.PersonRepository;
import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.data.domain.Pageable;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PersonRepositoryTests {

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

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private TestEntityManager entityManager;

    Person person;

    @BeforeEach
    public void setUp() {
        person = createAndSavePerson(1);
    }



    /* ----------------------------- *
     * FIND BY ID TESTS              *
     * ----------------------------- *
     */

    @Test
    public void testWhenCreatePersonAndFindById_thenReturnSamePerson() {

        Optional<Person> res = personRepository.findById(person.getId());
        assertThat(res.isPresent()).isTrue();
        assertThat(res.get()).isEqualTo(person);
    }

    @Test
    public void testWhenFindByInvalidId_thenReturnNull() {
        Optional<Person> res = personRepository.findById(-1L);
        assertThat(res.isPresent()).isFalse();
    }

    /* ----------------------------- *
     * FIND BY EMAIL TESTS           *
     * ----------------------------- *
     */

    @Test
    public void testWhenFindByEmail_whenInvalidEmail_thenReturnEmptyOptional() {
        Optional<Person> res = personRepository.findByEmail("invalid");
        assertThat(res.isPresent()).isFalse();
    }

    @Test
    public void testWhenFindByEmail_whenValidEmail_thenReturnEmptyOptional() {
        Optional<Person> res = personRepository.findByEmail("email1@email.com");
        assertThat(res.isPresent()).isTrue();
        assertThat(res.get().getId()).isEqualTo(person.getId());
    }



    /* -- helper -- */
    private Person createAndSavePerson(int i) {
        Person p = new Rider("name" + i, "pwdpwdpwdpwdpwd" + i, "email" + i + "@email.com");
        entityManager.persistAndFlush(p);
        return p;
    }
}
