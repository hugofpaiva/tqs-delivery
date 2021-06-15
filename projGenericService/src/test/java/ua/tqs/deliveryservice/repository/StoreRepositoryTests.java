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
import ua.tqs.deliveryservice.model.Address;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.model.Store;
import ua.tqs.deliveryservice.repository.RiderRepository;
import ua.tqs.deliveryservice.repository.StoreRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class StoreRepositoryTests {

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
    private StoreRepository storeRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testWhenCreateStoreAndFindById_thenReturnSameStore() {
        Store s = createAndSaveStore(1);

        Optional<Store> res = storeRepository.findById(s.getId());
        assertThat(res.isPresent()).isTrue();
        assertThat(res.get()).isEqualTo(s);
    }

    @Test
    public void testWhenFindByInvalidId_thenReturnNull() {
        Optional<Store> res = storeRepository.findById(-1L);
        assertThat(res.isPresent()).isFalse();
    }


    /* -- helper -- */
    private Store createAndSaveStore(int i) {
        Address addr_store = new Address("Street One, n. "+ i, "0000-00"+i, "Aveiro", "Portugal");
        Store s = new Store("store"+i, "the best store #"+i, "hard-pwd"+i, addr_store);
        entityManager.persist(addr_store);
        entityManager.persistAndFlush(s);
        return s;
    }


}
