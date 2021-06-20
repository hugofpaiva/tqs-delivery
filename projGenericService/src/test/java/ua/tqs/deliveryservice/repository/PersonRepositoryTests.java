package ua.tqs.deliveryservice.repository;

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
import ua.tqs.deliveryservice.model.Person;
import ua.tqs.deliveryservice.model.Rider;
import static org.assertj.core.api.Assertions.assertThat;


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




    /* ----------------------------- *
     * FIND BY ID TESTS              *
     * ----------------------------- *
     */

    @Test
    public void testWhenCreatePersonAndFindById_thenReturnSamePerson() {
        Person person = createAndSavePerson(1);
        Optional<Person> res = personRepository.findById(person.getId());
        assertThat(res.isPresent()).isTrue();
        assertThat(res.get()).isEqualTo(person);
    }

    @Test
    public void testWhenFindByInvalidId_thenReturnNull() {
        Person person = createAndSavePerson(1);
        Optional<Person> res = personRepository.findById(-1L);
        assertThat(res.isPresent()).isFalse();
    }

    /* ------------------------------------------------- *
     * FIND BY ALL TESTS                                  *
     * ------------------------------------------------- *
     */

    @Test
    public void testGivenPeopleAndFindByAll_thenReturnSamePeople() {
        Person p1 = createAndSavePerson(1);
        Person p2 = createAndSavePerson(2);

        List<Person> all = personRepository.findAll();

        assertThat(all).isNotNull();
        assertThat(all)
                .hasSize(2)
                .extracting(Person::getId)
                .contains(p1.getId(), p2.getId());
    }

    @Test
    public void testGivenNoPeople_whenFindAll_thenReturnEmpty() {
        List<Person> all = personRepository.findAll();
        assertThat(all).isNotNull();
        assertThat(all).hasSize(0);
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
        Person p = createAndSavePerson(1);
        Optional<Person> res = personRepository.findByEmail(p.getEmail());
        assertThat(res.isPresent()).isTrue();
        assertThat(res.get().getId()).isEqualTo(p.getId());
    }



    /* -- helper -- */
    private Person createAndSavePerson(int i) {
        Person p = new Rider("name" + i, "pwdpwdpwdpwdpwd" + i, "email_" + i + "@email.com");
        entityManager.persistAndFlush(p);
        return p;
    }
}
