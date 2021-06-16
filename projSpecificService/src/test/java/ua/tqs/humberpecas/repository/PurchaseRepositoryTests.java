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

import java.util.ArrayList;
import java.util.List;
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
            .withDatabaseName("shop");


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
        Person p = new Person("personName"+i, "pwdpwdpwd"+i, "email"+i+"@email.com");
        Address address = new Address("Street One, n. "+ i, "0000-00"+i, "Aveiro", "Portugal");

        List<Product> products = new ArrayList<>();
        products.add(new Product("hammer", 10.50, Category.SCREWDRIVER , "the best hammer", "image_url"));
        products.add(new Product("hammer v2", 20.50, Category.SCREWDRIVER , "the best hammer 2.0", "image_url"));

        Purchase purch = new Purchase(p, address, products);

        for (Product prod : products) {
            entityManager.persist(prod);
        }
        entityManager.persist(p);
        entityManager.persist(address);
        entityManager.persistAndFlush(purch);
        return purch;
    }

}
