package ua.tqs.deliveryservice.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ua.tqs.deliveryservice.model.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
 class PurchaseRepositoryTests {

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
    private PurchaseRepository purchaseRepository;

    @Autowired
    private TestEntityManager entityManager;

    /* ----------------------------- *
     * FIND BY ID TESTS              *
     * ----------------------------- *
     */


    @Test
     void testWhenCreatePurchaseAndFindById_thenReturnSamePurchase() {
        Purchase p = createAndSavePurchase(1, true);

        Optional<Purchase> res = purchaseRepository.findById(p.getId());
        assertThat(res).isPresent().contains(p);
    }

    @Test
     void testWhenFindByInvalidId_thenReturnNull() {
        Optional<Purchase> res = purchaseRepository.findById(-1L);
        assertThat(res).isNotPresent();
    }

    /* ------------------------------------------------- *
     * FIND BY ALL TESTS                                  *
     * ------------------------------------------------- *
     */

    @Test
     void testGivenPurchasesAndFindByAll_thenReturnSameRiders() {
        Purchase p1 = createAndSavePurchase(1, true);
        Purchase p2 = createAndSavePurchase(2, false);

        List<Purchase> all = purchaseRepository.findAll();

        assertThat(all).isNotNull();
        assertThat(all)
                .hasSize(2)
                .extracting(Purchase::getId)
                .contains(p1.getId(), p2.getId());
    }

    @Test
     void testGivenNoPurchases_whenFindAll_thenReturnEmpty() {
        List<Purchase> all = purchaseRepository.findAll();
        assertThat(all).isNotNull().isEmpty();
    }



    /* ------------------------------------------------- *
     * FIND ORDER IN WHICH RIDER IS NULL TESTS           *
     * ------------------------------------------------- *
     */

    @Test
     void testFindTopByRiderIsNullOrderByDate_whenAllGood() {
        createAndSavePurchase(1, true);
        Purchase p2 = createAndSavePurchase(2, false);
        createAndSavePurchase(3, true);

        Optional<Purchase> res = purchaseRepository.findTopByRiderIsNullOrderByDate();

        assertThat(res).isPresent().contains(p2);
    }

    @Test
     void testFindTopByRiderIsNullOrderByDate_whenNoResults() {
        createAndSavePurchase(1, true);
        createAndSavePurchase(2, true);
        Optional<Purchase> res = purchaseRepository.findTopByRiderIsNullOrderByDate();
        assertThat(res).isNotPresent();
    }

    @Test
     void testFindTopByRiderIsNullOrderByDate_whenNoPurchases() {
        Optional<Purchase> res = purchaseRepository.findTopByRiderIsNullOrderByDate();
        assertThat(res).isNotPresent();
    }

    /* ------------------------------------------------- *
     * FIND TOP GIVEN RIDER AND STATUS TESTS             *
     * ------------------------------------------------- *
     */

    @Test
     void testFindTopByRiderAndStatusIsNot_whenRiderHasNoSuch_returnEmpty() {
        Purchase p1 = createAndSavePurchase(1, true);

        Optional<Purchase> res = purchaseRepository.findTopByRiderAndStatusIsNot(p1.getRider(), p1.getStatus());
        assertThat(res).isNotPresent();
    }

    @Test
     void testFindTopByRiderAndStatusIsNot_whenRiderHasNoPurchase_returnEmpty() {
        createAndSavePurchase(1, true);
        Rider r = new Rider("new", "iahçdoihsaf", "new.rider@email.com");
        entityManager.persist(r);
        Optional<Purchase> res = purchaseRepository.findTopByRiderAndStatusIsNot(r, Status.PENDENT);
        assertThat(res).isNotPresent();
    }

    @Test
     void testFindTopByRiderAndStatusIsNot_whenRiderHasSuchPurchase_returnPurchase() {
        Purchase p1 = createAndSavePurchase(1, true);

        Optional<Purchase> res = purchaseRepository.findTopByRiderAndStatusIsNot(p1.getRider(), Status.PICKED_UP);
        assertThat(res).isPresent().contains(p1);
    }


    /* ------------------------------------------------- *
     * FIND ALL BY RIDER (PAGEABLE) TESTS          *
     * ------------------------------------------------- *
     */

    @Test
     void testFindAllByRiderWithPage_whenRiderHasNoPurchase_returnEmpty() {
        Rider r = new Rider("rider__", "gvhjbknutcfyvgkupwd", "riderrr@email.com");
        entityManager.persist(r);

        Pageable paging = PageRequest.of(0, 10, Sort.by("date").descending());
        Page<Purchase> res = purchaseRepository.findAllByRider(r, paging);
        assertThat(res).isNotNull();
        assertThat(res.getTotalElements()).isZero();
        assertThat(res.getTotalPages()).isZero();
    }

    @Test
     void testFindAllByRiderWithPage_whenRiderHasPurchases_returnPage() {
        Purchase p1 = createAndSavePurchase(1, true);
        Purchase p2 = createAndSavePurchase(2, false);
        Purchase p3 = createAndSavePurchase(3, false);
        p2.setRider(p1.getRider());
        p3.setRider(p1.getRider());

        Pageable paging = PageRequest.of(0, 2, Sort.by("date").descending());
        Page<Purchase> res = purchaseRepository.findAllByRider(p1.getRider(), paging);
        assertThat(res).isNotNull();
        assertThat(res.getTotalElements()).isEqualTo(3);
        assertThat(res.getTotalPages()).isEqualTo(2);
        assertThat(res).extracting(Purchase::getId).contains(p2.getId(), p3.getId());
        assertThat(res).extracting(Purchase::getId).doesNotContain(p1.getId());
    }

    @Test
     void testFindAllByRiderWithEmptyPage_whenRiderHasPurchases_returnPage() {
        Purchase p1 = createAndSavePurchase(1, true);
        Purchase p2 = createAndSavePurchase(2, false);
        Purchase p3 = createAndSavePurchase(3, false);
        p2.setRider(p1.getRider());
        p3.setRider(p1.getRider());

        Pageable paging = PageRequest.of(5, 2, Sort.by("date").descending());
        Page<Purchase> res = purchaseRepository.findAllByRider(p1.getRider(), paging);
        assertThat(res).isNotNull();
        assertThat(res.getTotalElements()).isEqualTo(3);
        assertThat(res.getTotalPages()).isEqualTo(2);
        assertThat(res).isEmpty();
    }


    /* ------------------------------------------------- *
     * FIND TOP ORDER BY DATE TESTS                      *
     * ------------------------------------------------- *
     */

    @Test
     void testFindTopByOrderByDate_whenNoPurchase_returnEmpty() {
        Optional<Purchase> res = purchaseRepository.findTopByOrderByDate();
        assertThat(res).isNotPresent();
    }

    @Test
     void testFindTopByOrderByDate_whenPurchases_returnFirst() {
        Purchase p1 = createAndSavePurchase(1, true);
        createAndSavePurchase(2, true);

        Optional<Purchase> res = purchaseRepository.findTopByOrderByDate();
        assertThat(res).isPresent().contains(p1);
    }

    /* ------------------------------------------------- *
     * COUNT PURCHASES GIVEN STORE TESTS                 *
     * ------------------------------------------------- *
     */

    @Test
     void testCountPurchaseByStore_givenStoreWithoutPurchases_return0() {
        Address addr_store = new Address("Street One, n. 342", "0000-002", "Aveiro", "Portugal");
        Store s = new Store("storeeee", "the best store .", "hard-pwddfsdf", addr_store);
        entityManager.persist(addr_store);
        entityManager.persist(s);

        Long count = purchaseRepository.countPurchaseByStore(s);
        assertThat(count).isZero();
    }

    @Test
     void testCountPurchaseByStore_givenStoreWithPurchases_returnNoOfPurchases() {
        Purchase p = createAndSavePurchase(1, true);

        Long count = purchaseRepository.countPurchaseByStore(p.getStore());
        assertThat(count).isEqualTo(1);
    }


    /* ------------------------------------------------- *
     * COUNT PURCHASES GIVEN STATUS TESTS                 *
     * ------------------------------------------------- *
     */

    @Test
     void testCountPurchaseByStatus_givenNoPurchases_return0() {
        Long count = purchaseRepository.countPurchaseByStatusIs(Status.ACCEPTED);
        assertThat(count).isZero();
    }

    @Test
     void testCountPurchaseByStatus_givenStatusWithoutPurchases_return0() {
        createAndSavePurchase(1, true);
        Long count = purchaseRepository.countPurchaseByStatusIs(Status.PICKED_UP);
        assertThat(count).isZero();
    }

    @Test
     void testCountPurchaseByStatus_givenStatusWithPurchases_returnNoOfPurchases() {
        createAndSavePurchase(1, false);
        createAndSavePurchase(2, true);
        createAndSavePurchase(3, true);

        Long count = purchaseRepository.countPurchaseByStatusIs(Status.ACCEPTED);
        assertThat(count).isEqualTo(2);
    }

    /* ------------------------------------------------- *
     *       GET AVERAGE REVIEW TESTS                    *
     * ------------------------------------------------- *
     */

    @Test
     void testWhenGetAverageReview_givenNoPurchase_thenReturnNull() {
        Long[] res = purchaseRepository.getSumDeliveryTimeAndCountPurchases().get(0);

        assertThat(res).isNotNull().hasSize(2);
        assertThat(res[0]).isNull();
        assertThat(res[1]).isZero();

    }

    @Test
     void testWhenGetAverageReview_givenPurchasesNotDelievered_thenReturnNull() {
        createAndSavePurchase(1, true);
        createAndSavePurchase(2, true);

        Long[] res = purchaseRepository.getSumDeliveryTimeAndCountPurchases().get(0);

        assertThat(res).isNotNull().hasSize(2);
        assertThat(res[0]).isNull();
        assertThat(res[1]).isZero();
    }

    @Test
     void testWhenGetSumReviewsAndQuantity_givenReviews_thenReturnSums() {
        Purchase p1 = createAndSavePurchase(1, true);
        p1.setStatus(Status.DELIVERED);
        p1.setDeliveryTime(30L);

        Purchase p2 = createAndSavePurchase(2, false);
        p2.setStatus(Status.DELIVERED);
        p2.setDeliveryTime(15L);

        createAndSavePurchase(3, false);

        Long[] res = purchaseRepository.getSumDeliveryTimeAndCountPurchases().get(0);

        assertThat(res).isNotNull().hasSize(2);
        assertThat(res[0]).isEqualTo(45L);
        assertThat(res[1]).isEqualTo(2);
    }

    /* ------------------------------------------------- *
     *       GET TOP 5 CITIES TESTS                      *
     * ------------------------------------------------- *
     */

    @Test
     void testGetTop5Cities_whenNoPurchases_thenReturn() {
        List<Object[]> res = purchaseRepository.getTopFiveCitiesOfPurchases();

        assertThat(res).isNotNull().isEmpty();
    }

    @Test
     void testGetTop5Cities_when5DifferentCitiesDontExistInPurchases_thenReturn() {
        Purchase p1 = createAndSavePurchaseForTop5Cities(1, true);
        Purchase p2 = createAndSavePurchaseForTop5Cities(2, true);

        List<Object[]> res = purchaseRepository.getTopFiveCitiesOfPurchases();

        assertThat(res).isNotNull();
        assertThat(res.size()).isEqualTo(2);

        assertThat(res.get(0)[0]).isEqualTo(p2.getAddress().getCity());
        assertThat(res.get(0)[1]).isEqualTo(1L);
        assertThat(res.get(1)[0]).isEqualTo(p1.getAddress().getCity());
        assertThat(res.get(1)[1]).isEqualTo(1L);
    }

    @Test
     void testGetTop5Cities_when5DifferentCities_thenReturn() {
        Purchase p1 = createAndSavePurchaseForTop5Cities(1, true);
        Purchase p2 = createAndSavePurchaseForTop5Cities(2, true);
        Purchase p3 = createAndSavePurchaseForTop5Cities(3, true);
        Purchase p4 = createAndSavePurchaseForTop5Cities(4, true);
        Purchase p5 = createAndSavePurchaseForTop5Cities(5, true);

        List<Object[]> res = purchaseRepository.getTopFiveCitiesOfPurchases();

        assertThat(res).isNotNull();
        assertThat(res.size()).isEqualTo(5);

        assertThat(res.get(0)[0]).isEqualTo(p2.getAddress().getCity());
        assertThat(res.get(0)[1]).isEqualTo(1L);

        assertThat(res.get(1)[0]).isEqualTo(p4.getAddress().getCity());
        assertThat(res.get(1)[1]).isEqualTo(1L);

        assertThat(res.get(2)[0]).isEqualTo(p1.getAddress().getCity());
        assertThat(res.get(2)[1]).isEqualTo(1L);

        assertThat(res.get(3)[0]).isEqualTo(p5.getAddress().getCity());
        assertThat(res.get(3)[1]).isEqualTo(1L);

        assertThat(res.get(4)[0]).isEqualTo(p3.getAddress().getCity());
        assertThat(res.get(4)[1]).isEqualTo(1L);

    }

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

    /* -- helper -- */
    private Purchase createAndSavePurchaseForTop5Cities(int i, boolean rider) {
        Address addr_store = new Address("Street One, n. "+ i, "0000-00"+i, "Aveiro"+i, "Portugal");
        Store s = new Store("store"+i, "the best store #"+i, "hard-pwd"+i, addr_store);
        Address addr_purchase = new Address("Street Twooo, n. "+ i, "1100-00"+i, "Aveiro"+i, "Portugal");
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
