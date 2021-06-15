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
public class ProductRepositoryTests {
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
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testWhenCreateProductAndFindById_thenReturnSameProduct() {
        Product p = createAndSaveProduct(1);

        Optional<Product> res = productRepository.findById(p.getId());
        assertThat(res.isPresent()).isTrue();
        assertThat(res.get()).isEqualTo(p);
    }

    @Test
    public void testWhenFindByInvalidId_thenReturnNull() {
        Optional<Product> res = productRepository.findById(-1L);
        assertThat(res.isPresent()).isFalse();
    }


    /* -- helper -- */
    private Product createAndSaveProduct(int i) {
        Product p = new Product("hammer", 10.50, Category.SCREWDRIVER , "the best hammer", 3);

        entityManager.persistAndFlush(p);
        return p;
    }

}
