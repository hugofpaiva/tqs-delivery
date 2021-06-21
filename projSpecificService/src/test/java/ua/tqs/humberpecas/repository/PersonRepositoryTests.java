package ua.tqs.humberpecas.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ua.tqs.humberpecas.model.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PersonRepositoryTests {
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

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testWhenCreatePersonAndFindById_thenReturnSamePerson() {
        Person p = createAndSavePerson(1);

        Optional<Person> res = personRepository.findById(p.getId());
        assertThat(res.isPresent()).isTrue();
        assertThat(res.get()).isEqualTo(p);
    }

    @Test
    public void testWhenFindByInvalidId_thenReturnNull() {
        Optional<Person> res = personRepository.findById(-1L);
        assertThat(res.isPresent()).isFalse();
    }

    /* -- helper -- */
    private Person createAndSavePerson(int i) {
        Person p = new Person("name" + i, "pwd12345" + i, "email" + i + "@email.com");
        entityManager.persistAndFlush(p);
        return p;
    }
}
