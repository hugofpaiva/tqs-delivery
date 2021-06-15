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
import ua.tqs.deliveryservice.model.Manager;
import ua.tqs.deliveryservice.model.Store;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ManagerRepositoryTests {

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
    private ManagerRepository managerRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testWhenCreateManagerAndFindById_thenReturnSameManager() {
        Manager m = createAndSaveManager(1);

        Optional<Manager> res = managerRepository.findById(m.getId());
        assertThat(res.isPresent()).isTrue();
        assertThat(res.get()).isEqualTo(m);
    }

    @Test
    public void testWhenFindByInvalidId_thenReturnNull() {
        Optional<Manager> res = managerRepository.findById(-1L);
        assertThat(res.isPresent()).isFalse();
    }


    /* -- helper -- */
    private Manager createAndSaveManager(int i) {
        Manager m = new Manager("managerName"+i, "managerPwd"+i, "manager"+i+"@email.com");
        entityManager.persistAndFlush(m);
        return m;
    }


}
