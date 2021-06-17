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
import ua.tqs.humberpecas.model.Category;
import ua.tqs.humberpecas.model.Person;
import ua.tqs.humberpecas.model.Product;
import ua.tqs.humberpecas.model.ShoppingCart;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ShoppingCartRepositoryTests {
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
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testWhenCreateShoppingCartAndFindById_thenReturnSameShoppingCart() {
        ShoppingCart sc = createAndSaveShoppingCart(1);

        Optional<ShoppingCart> res = shoppingCartRepository.findById(sc.getId());
        assertThat(res.isPresent()).isTrue();
        assertThat(res.get()).isEqualTo(sc);
    }

    @Test
    public void testWhenFindByInvalidId_thenReturnNull() {
        Optional<ShoppingCart> res = shoppingCartRepository.findById(-1L);
        assertThat(res.isPresent()).isFalse();
    }


    /* -- helper -- */
    private ShoppingCart createAndSaveShoppingCart(int i) {
        List<Product> products = new ArrayList<>();
        products.add(new Product(10.50, "hammer","the best hammer", Category.SCREWDRIVER ));
        products.add(new Product(20.50, "hammer v2", "the best hammer 2.0", Category.SCREWDRIVER ));

        ShoppingCart sc = new ShoppingCart(products);

        for (Product prod : products) {
            entityManager.persist(prod);
        }
        entityManager.persistAndFlush(sc);
        return sc;
    }

}
