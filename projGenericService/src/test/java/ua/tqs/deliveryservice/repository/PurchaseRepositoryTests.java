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
import ua.tqs.deliveryservice.repository.PersonRepository;
import ua.tqs.deliveryservice.repository.PurchaseRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PurchaseRepositoryTests {

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
    private PurchaseRepository purchaseRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testWhenCreatePurchaseAndFindById_thenReturnSamePurchase() {
        Purchase p = createAndSavePurchase(1);

        Optional<Purchase> res = purchaseRepository.findById(p.getId());
        assertThat(res.isPresent()).isTrue();
        assertThat(res.get()).isEqualTo(p);
    }

    @Test
    public void testWhenFindByInvalidId_thenReturnNull() {
        Optional<Purchase> res = purchaseRepository.findById(-1L);
        assertThat(res.isPresent()).isFalse();
    }


    /* -- helper -- */
    private Purchase createAndSavePurchase(int i) {
        Rider r = new Rider("rider"+i, "pwd"+i, "rider"+i+"@email.com");
        Address addr_store = new Address("Street One, n. "+ i, "0000-00"+i, "Aveiro", "Portugal");
        Store s = new Store("store"+i, "the best store #"+i, "hard-pwd"+i, addr_store);

        Address addr_purchase = new Address("Street Twooo, n. "+ i, "1100-00"+i, "Aveiro", "Portugal");
        Purchase p = new Purchase(addr_purchase, r, s, "João");

        entityManager.persist(r);
        entityManager.persist(addr_store);
        entityManager.persist(s);
        entityManager.persist(addr_purchase);
        entityManager.persistAndFlush(p);
        return p;
    }


}
