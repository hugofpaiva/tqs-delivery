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
import ua.tqs.deliveryservice.model.*;

import java.util.List;
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
            .withDatabaseName("delivery");

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

    /* ------------------------------------------------- *
     * FIND BY ALL TESTS                                  *
     * ------------------------------------------------- *
     */

    @Test
    public void testGivenAddressesAndFindByAll_thenReturnSameAddresses() {
        Address a1 = createAndSaveAddress(1);
        Address a2 = createAndSaveAddress(2);

        List<Address> all = addressRepository.findAll();

        assertThat(all).isNotNull();
        assertThat(all)
                .hasSize(2)
                .extracting(Address::getId)
                .contains(a1.getId(), a2.getId());
    }

    @Test
    public void testGivenNoAddresses_whenFindAll_thenReturnEmpty() {
        List<Address> all = addressRepository.findAll();
        assertThat(all).isNotNull();
        assertThat(all).hasSize(0);
    }


    /* -- helper -- */
    private Address createAndSaveAddress(int i) {
        Address a = new Address("Street One, n. "+ i, "0000-00"+i, "Aveiro", "Portugal");
        entityManager.persistAndFlush(a);
        return a;
    }


}
