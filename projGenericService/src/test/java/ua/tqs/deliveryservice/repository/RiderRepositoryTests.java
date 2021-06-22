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
class RiderRepositoryTests {

    @Container
    static PostgreSQLContainer container = new PostgreSQLContainer("postgres:11.12")
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
    void testWhenCreateRiderAndFindById_thenReturnSameRider() {
        Rider r = createAndSaveRider(1);

        Optional<Rider> res = riderRepository.findById(r.getId());
        assertThat(res).isPresent().contains(r);
    }

    @Test
    void testWhenFindByInvalidId_thenReturnNull() {
        Optional<Rider> res = riderRepository.findById(-1L);
        assertThat(res).isNotPresent();
    }


    /* ------------------------------------------------- *
     * FIND BY EMAIL TESTS                               *
     * ------------------------------------------------- *
     */

    @Test
    void testWhenCreateRiderAndFindByEmail_thenReturnSameRider() {
        Rider r = createAndSaveRider(1);

        Optional<Rider> res = riderRepository.findByEmail(r.getEmail());
        assertThat(res).isPresent().contains(r);
    }

    @Test
    void testWhenFindByInvalidEmail_thenReturnEmpty() {
        Optional<Rider> res = riderRepository.findByEmail("invalid");
        assertThat(res).isNotPresent();
    }

    /* ------------------------------------------------- *
     * FIND BY ALL TESTS                                  *
     * ------------------------------------------------- *
     */

    @Test
    void testWhenCreateRidersAndFindByAll_thenReturnSameRiders() {
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
    void testGivenNoRiders_whenFindAll_thenReturnEmpty() {
        List<Rider> all = riderRepository.findAll();
        assertThat(all).isNotNull().isEmpty();
    }


    /* ------------------------------------------------- *
     * GET AVERAGE REVIEW TESTS                          *
     * ------------------------------------------------- *
     */

    @Test
    void testWhenGetSumReviewsAndQuantity_givenNoRiders_thenReturnNull() {
        Double res = riderRepository.getAverageRiderRating();
        assertThat(res).isNull();
    }

    @Test
    void testWhenGetSumReviewsAndQuantity_givenRidersWithoutReviews_thenReturnNull() {
        createAndSaveRider(1);
        createAndSaveRider(2);
        Double res = riderRepository.getAverageRiderRating();
        assertThat(res).isNull();
    }

    @Test
    void testWhenGetSumReviewsAndQuantity_givenRidersWithReviewsWith0_thenReturnNull() {
        createAndSaveRider(1);
        Rider r = createAndSaveRider(2);
        r.setTotalNumReviews(2);
        r.setReviewsSum(0);
        Double res = riderRepository.getAverageRiderRating();
        assertThat(res).isNotNull().isZero();
    }

    @Test
    void testWhenGetSumReviewsAndQuantity_givenReviews_thenReturnSums() {
        Rider r = createAndSaveRider(1);
        r.setReviewsSum(32);
        r.setTotalNumReviews(10);

        Rider r2 = createAndSaveRider(2);
        r2.setReviewsSum(1);
        r2.setTotalNumReviews(1);

        Double res = riderRepository.getAverageRiderRating();

        assertThat(res).isNotNull().isEqualTo((double) (32/10 + 1)/2);
    }


    /* -- helper -- */
    private Rider createAndSaveRider(int i) {
        Rider r = new Rider("rider"+i, "pwddddddd"+i, "rider"+i+"@email.com");
        entityManager.persistAndFlush(r);
        return r;
    }


}
