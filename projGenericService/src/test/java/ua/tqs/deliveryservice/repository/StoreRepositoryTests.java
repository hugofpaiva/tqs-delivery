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

import java.util.List;
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


    /* ------------------------------------------------- *
     * FIND BY ID TESTS                                  *
     * ------------------------------------------------- *
     */

    @Test
    public void testWhenCreateStoreAndFindById_thenReturnSameStore() {
        Store s = createAndSaveStore(1);

        Optional<Store> res = storeRepository.findById(s.getId());
        assertThat(res.isPresent()).isTrue();
        assertThat(res.get()).isEqualTo(s);
    }

    @Test
    public void testWhenFindByInvalidId_thenReturnEmpty() {
        Optional<Store> res = storeRepository.findById(-1L);
        assertThat(res.isPresent()).isFalse();
    }


    /* ------------------------------------------------- *
     * FIND BY ALL TESTS                                  *
     * ------------------------------------------------- *
     */

    @Test
    public void testWhenCreateStoresAndFindByAll_thenReturnSameStores() {
        Store s1 = createAndSaveStore(1);
        Store s2 = createAndSaveStore(2);

        List<Store> all = storeRepository.findAll();

        assertThat(all).isNotNull();
        assertThat(all)
                .hasSize(2)
                .extracting(Store::getId)
                .contains(s1.getId(), s2.getId());
    }

    @Test
    public void testGivenNoStores_whenFindAll_thenReturnEmpty() {
        List<Store> all = storeRepository.findAll();
        assertThat(all).isNotNull();
        assertThat(all).hasSize(0);
    }


    /* ------------------------------------------------- *
     * FIND BY EMAIL TESTS                               *
     * ------------------------------------------------- *
     */

    @Test
    public void testWhenCreateStoreAndFindByToken_thenReturnSameStore() {
        Store s = createAndSaveStore(1);

        Optional<Store> res = storeRepository.findByToken(s.getToken());
        assertThat(res.isPresent()).isTrue();
        assertThat(res.get()).isEqualTo(s);
    }

    @Test
    public void testWhenFindByInvalidToken_thenReturnEmpty() {
        Optional<Store> res = storeRepository.findByToken("invalid-token");
        assertThat(res.isPresent()).isFalse();
    }


    /* -- helper -- */
    private Store createAndSaveStore(int i) {
        Address addr_store = new Address("Street One, n. "+ i, "0000-00"+i, "Aveiro", "Portugal");
        Store s = new Store("store"+i, "the best store #"+i, "hard-pwd"+i, addr_store, "http://localhost:808"+i+"/delivery/");
        entityManager.persist(addr_store);
        entityManager.persistAndFlush(s);
        return s;
    }


}
