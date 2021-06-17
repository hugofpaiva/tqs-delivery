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
import ua.tqs.deliveryservice.model.Rider;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RiderRepositoryTests {

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
    private RiderRepository riderRepository;

    @Autowired
    private TestEntityManager entityManager;


    /* ------------------------------------------------- *
     * FIND BY ID TESTS                                  *
     * ------------------------------------------------- *
     */

    @Test
    public void testWhenCreateRiderAndFindById_thenReturnSameRider() {
        Rider r = createAndSaveRider(1);

        Optional<Rider> res = riderRepository.findById(r.getId());
        assertThat(res.isPresent()).isTrue();
        assertThat(res.get()).isEqualTo(r);
    }

    @Test
    public void testWhenFindByInvalidId_thenReturnNull() {
        Optional<Rider> res = riderRepository.findById(-1L);
        assertThat(res.isPresent()).isFalse();
    }


    /* ------------------------------------------------- *
     * FIND BY EMAIL TESTS                               *
     * ------------------------------------------------- *
     */

    @Test
    public void testWhenCreateRiderAndFindByEmail_thenReturnSameRider() {
        Rider r = createAndSaveRider(1);

        Optional<Rider> res = riderRepository.findByEmail(r.getEmail());
        assertThat(res.isPresent()).isTrue();
        assertThat(res.get()).isEqualTo(r);
    }

    @Test
    public void testWhenFindByInvalidEmail_thenReturnEmpty() {
        Optional<Rider> res = riderRepository.findByEmail("invalid");
        assertThat(res.isPresent()).isFalse();
    }

    /* ------------------------------------------------- *
     * FIND BY ALL TESTS                                  *
     * ------------------------------------------------- *
     */

    @Test
    public void testWhenCreateRidersAndFindByAll_thenReturnSameRiders() {
        Rider r1 = createAndSaveRider(1);
        Rider r2 = createAndSaveRider(2);

        List<Rider> all = riderRepository.findAll();

        assertThat(all).isNotNull();
        assertThat(all)
                .hasSize(2)
                .extracting(Rider::getId)
                .contains(r1.getId(), r2.getId());
    }

    @Test
    public void testGivenNoRiders_whenFindAll_thenReturnEmpty() {
        List<Rider> all = riderRepository.findAll();
        assertThat(all).isNotNull();
        assertThat(all).hasSize(0);
    }


    /* ------------------------------------------------- *
     * GET AVERAGE REVIEW TESTS                          *
     * ------------------------------------------------- *
     */

    @Test
    public void testWhenGetSumReviewsAndQuantity_givenNoRiders_thenReturnNull() {
        Long[] res = riderRepository.getSumReviewsAndQuantity().get(0);

        assertThat(res).isNotNull();
        assertThat(res.length).isEqualTo(2);
        assertThat(res[0]).isNull();
        assertThat(res[1]).isNull();

    }

    @Test
    public void testWhenGetSumReviewsAndQuantity_givenRidersWithoutReviews_thenReturn0s() {
        createAndSaveRider(1);
        createAndSaveRider(2);

        Long[] res = riderRepository.getSumReviewsAndQuantity().get(0);

        assertThat(res).isNotNull();
        assertThat(res.length).isEqualTo(2);
        assertThat(res[0]).isEqualTo(0);
        assertThat(res[1]).isEqualTo(0);
    }


    @Test
    public void testWhenGetSumReviewsAndQuantity_givenReviews_thenReturnSums() {
        Rider r = createAndSaveRider(1);
        r.setReviewsSum(32);
        r.setTotalNumReviews(10);

        Rider r2 = createAndSaveRider(2);
        r2.setReviewsSum(1);
        r2.setTotalNumReviews(1);

        Long[] res = riderRepository.getSumReviewsAndQuantity().get(0);

        assertThat(res).isNotNull();
        assertThat(res.length).isEqualTo(2);
        assertThat(res[0]).isEqualTo(33);
        assertThat(res[1]).isEqualTo(11);
    }



    /* -- helper -- */
    private Rider createAndSaveRider(int i) {
        Rider r = new Rider("rider"+i, "pwddddddd"+i, "rider"+i+"@email.com");
        entityManager.persistAndFlush(r);
        return r;
    }


}
