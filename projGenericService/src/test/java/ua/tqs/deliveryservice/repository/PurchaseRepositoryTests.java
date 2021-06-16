package ua.tqs.deliveryservice.repository;

import com.github.dockerjava.api.command.PullImageResultCallback;
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

    /* ----------------------------- *
     * FIND BY ID TESTS              *
     * ----------------------------- *
     */


    @Test
    public void testWhenCreatePurchaseAndFindById_thenReturnSamePurchase() {
        Purchase p = createAndSavePurchase(1, true);

        Optional<Purchase> res = purchaseRepository.findById(p.getId());
        assertThat(res.isPresent()).isTrue();
        assertThat(res.get()).isEqualTo(p);
    }

    @Test
    public void testWhenFindByInvalidId_thenReturnNull() {
        Optional<Purchase> res = purchaseRepository.findById(-1L);
        assertThat(res.isPresent()).isFalse();
    }


    /* ------------------------------------------------- *
     * FIND OLDER IN WHICH RIDER IS NULL TESTS           *
     * ------------------------------------------------- *
     */

    @Test
    public void testFindTopByRiderIsNullOrderByDate_whenAllGood() {
        createAndSavePurchase(1, true);
        Purchase p2 = createAndSavePurchase(2, false);
        createAndSavePurchase(3, true);

        Optional<Purchase> res = purchaseRepository.findTopByRiderIsNullOrderByDate();

        assertThat(res.isPresent()).isTrue();
        assertThat(res.get()).isEqualTo(p2);
    }

    @Test
    public void testFindTopByRiderIsNullOrderByDate_whenNoResults() {
        createAndSavePurchase(1, true);
        createAndSavePurchase(2, true);
        Optional<Purchase> res = purchaseRepository.findTopByRiderIsNullOrderByDate();
        assertThat(res.isPresent()).isFalse();
    }

    @Test
    public void testFindTopByRiderIsNullOrderByDate_whenNoPurchases() {
        Optional<Purchase> res = purchaseRepository.findTopByRiderIsNullOrderByDate();
        assertThat(res.isPresent()).isFalse();
    }

    /* ------------------------------------------------- *
     * FIND TOP GIVEN RIDER AND STATUS TESTS             *
     * ------------------------------------------------- *
     */

    @Test
    public void testFindTopByRiderAndStatusIsNot_whenRiderHasNoSuch_returnEmpty() {
        Purchase p1 = createAndSavePurchase(1, true);

        Optional<Purchase> res = purchaseRepository.findTopByRiderAndStatusIsNot(p1.getRider(), p1.getStatus());
        assertThat(res.isPresent()).isFalse();
    }

    @Test
    public void testFindTopByRiderAndStatusIsNot_whenRiderHasNoPurchase_returnEmpty() {
        createAndSavePurchase(1, true);
        Rider r = new Rider("new", "iahçdoihsaf", "new.rider@email.com");
        entityManager.persist(r);
        Optional<Purchase> res = purchaseRepository.findTopByRiderAndStatusIsNot(r, Status.PENDENT);
        assertThat(res.isPresent()).isFalse();
    }

    @Test
    public void testFindTopByRiderAndStatusIsNot_whenRiderHasSuchPurchase_returnPurchase() {
        Purchase p1 = createAndSavePurchase(1, true);

        Optional<Purchase> res = purchaseRepository.findTopByRiderAndStatusIsNot(p1.getRider(), Status.PICKED_UP);
        assertThat(res.isPresent()).isTrue();
        assertThat(res.get()).isEqualTo(p1);
    }


    /* ------------------------------------------------- *
     * TODO: FIND ALL BY RIDER (PAGEABLE) TESTS                *
     * ------------------------------------------------- *
     */

    /* ------------------------------------------------- *
     * FIND TOP ORDER BY DATE TESTS                      *
     * ------------------------------------------------- *
     */

    @Test
    public void testFindTopByOrderByDate_whenNoPurchase_returnEmpty() {
        Optional<Purchase> res = purchaseRepository.findTopByOrderByDate();
        assertThat(res.isPresent()).isFalse();
    }

    @Test
    public void testFindTopByOrderByDate_whenPurchases_returnFirst() {
        Purchase p1 = createAndSavePurchase(1, true);
        createAndSavePurchase(2, true);

        Optional<Purchase> res = purchaseRepository.findTopByOrderByDate();
        assertThat(res.isPresent()).isTrue();
        assertThat(res.get()).isEqualTo(p1);
    }

    /* ------------------------------------------------- *
     * COUNT PURCHASES GIVEN STORE TESTS                 *
     * ------------------------------------------------- *
     */

    @Test
    public void testCountPurchaseByStore_givenStoreWithoutPurchases_return0() {
        Address addr_store = new Address("Street One, n. 342", "0000-002", "Aveiro", "Portugal");
        Store s = new Store("storeeee", "the best store .", "hard-pwddfsdf", addr_store);
        entityManager.persist(addr_store);
        entityManager.persist(s);

        Long count = purchaseRepository.countPurchaseByStore(s);
        assertThat(count).isEqualTo(0);
    }

    @Test
    public void testCountPurchaseByStore_givenStoreWithPurchases_returnNoOfPurchases() {
        Purchase p = createAndSavePurchase(1, true);

        Long count = purchaseRepository.countPurchaseByStore(p.getStore());
        assertThat(count).isEqualTo(1);
    }


    /* ------------------------------------------------- *
     * COUNT PURCHASES GIVEN STATUS TESTS                 *
     * ------------------------------------------------- *
     */

    @Test
    public void testCountPurchaseByStatus_givenNoPurchases_return0() {
        Long count = purchaseRepository.countPurchaseByStatusIs(Status.ACCEPTED);
        assertThat(count).isEqualTo(0);
    }

    @Test
    public void testCountPurchaseByStatus_givenStatusWithoutPurchases_return0() {
        createAndSavePurchase(1, true);
        Long count = purchaseRepository.countPurchaseByStatusIs(Status.PICKED_UP);
        assertThat(count).isEqualTo(0);
    }

    @Test
    public void testCountPurchaseByStatus_givenStatusWithPurchases_returnNoOfPurchases() {
        createAndSavePurchase(1, false);
        createAndSavePurchase(2, true);
        createAndSavePurchase(3, true);

        Long count = purchaseRepository.countPurchaseByStatusIs(Status.ACCEPTED);
        assertThat(count).isEqualTo(2);
    }

    /* ------------------------------------------------- *
     * TODO: GET AVERAGE REVIEW TESTS                    *
     * ------------------------------------------------- *
     */


    /* -- helper -- */
    private Purchase createAndSavePurchase(int i, boolean rider) {
        Address addr_store = new Address("Street One, n. "+ i, "0000-00"+i, "Aveiro", "Portugal");
        Store s = new Store("store"+i, "the best store #"+i, "hard-pwd"+i, addr_store);
        Address addr_purchase = new Address("Street Twooo, n. "+ i, "1100-00"+i, "Aveiro", "Portugal");
        Purchase p = new Purchase(addr_purchase, s, "João");

        if (rider) {
            Rider r = new Rider("rider"+i, "gvhjbknutcfyvgkupwd"+i, "rider"+i+"@email.com");
            entityManager.persist(r);
            p.setStatus(Status.ACCEPTED);
            p.setRider(r);
        }

        entityManager.persist(addr_store);
        entityManager.persist(s);
        entityManager.persist(addr_purchase);
        entityManager.persistAndFlush(p);
        return p;

    }


}
