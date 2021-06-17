package ua.tqs.humberpecas.repository;

import org.checkerframework.checker.units.qual.A;
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
import ua.tqs.humberpecas.model.Address;
import ua.tqs.humberpecas.model.Person;

import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AddressRepositoryTests {
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
    private AddressRepository addressRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PersonRepository personRepository;

    @Test
    public void testWhenCreateAddressAndFindById_thenReturnSameAddress() {
        Address a = createAndSaveAddress(1);

        Optional<Address> res = addressRepository.findById(a.getId());
        assertThat(res.isPresent()).isTrue();
        assertThat(res.get()).isEqualTo(a);
    }

    @Test
    public void testWhenFindByInvalidId_thenReturnNull() {
        Optional<Address> res = addressRepository.findById(-1L);
        assertThat(res.isPresent()).isFalse();
    }


    /* -- helper -- */
    private Address createAndSaveAddress(int i) {

        Person p = new Person("Fernando", "12345678","fernando@ua.pt");
        personRepository.saveAndFlush(p);
        Address a = new Address("Street One, n. "+ i, "0000-00"+i, "Aveiro", "Portugal", p);
        entityManager.persistAndFlush(a);
        return a;
    }

}
