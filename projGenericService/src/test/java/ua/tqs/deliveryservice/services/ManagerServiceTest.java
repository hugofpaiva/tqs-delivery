package ua.tqs.deliveryservice.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.model.*;
import ua.tqs.deliveryservice.repository.ManagerRepository;
import ua.tqs.deliveryservice.repository.PurchaseRepository;
import ua.tqs.deliveryservice.repository.RiderRepository;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {
    @Mock
    private ManagerRepository managerRepository;

    @Mock
    private RiderRepository riderRepository;

    @Mock
    private JwtUserDetailsService jwtUserDetailsService;

    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private ManagerService managerService;

    private Manager manager = new Manager("joao", "aRightPassword", "TQS_delivery@example.com");
    private Address address = new Address("Universidade de Aveiro", "3800-000", "Aveiro", "Portugal");
    private Rider rider = new Rider("Raquel", "aRightPassword", "TQS_delivery@ua.com");
    private Store store = new Store("HumberPecas", "Peça(s) rápido", "somestringnewtoken", this.address);
    private Purchase purchase = new Purchase(this.address, this.rider, this.store, "Joana");

    // --------------------------------------------
    // --      MANAGER: GET ALL RIDERS INFO      --
    // --------------------------------------------

    @Test
    public void testGetRidersButInvalidPageNo_thenThrow() {
        assertThrows(IllegalArgumentException.class, () -> {
            managerService.getRidersInformation(-1, 10);
        });
    }

    @Test
    public void testGetRidersButInvalidPageSize_thenThrow() {
        assertThrows(IllegalArgumentException.class, () -> {
            managerService.getRidersInformation(0, -1);
        });
    }

    @Test
    public void testGetRiderInfoEverythingValid_thenReturn1Record() throws InvalidLoginException {
        this.rider.setPurchases(Arrays.asList(this.purchase));
        this.rider.setReviewsSum(4);
        this.rider.setTotalNumReviews(1);

        List<Rider> riderList = Arrays.asList(this.rider);
        Page<Rider> pageRequest = new PageImpl(riderList, PageRequest.of(0, 10), riderList.size());

        Mockito.when(riderRepository.findAll(PageRequest.of(0, 10))).thenReturn(pageRequest);

        Map<String, Object> found = managerService.getRidersInformation(0, 10);

        Mockito.verify(riderRepository, VerificationModeFactory.times(1))
                .findAll(any(Pageable.class));

        assertThat(((List<Map<String, Object>>) found.get("riders"))).hasSize(1).extracting("name").contains(this.rider.getName());
        assertThat(((List<Map<String, Object>>) found.get("riders"))).hasSize(1).extracting("numberOrders").contains(1);
        assertThat(((List<Map<String, Object>>) found.get("riders"))).hasSize(1).extracting("average").contains(4.0);

        assertThat(found.get("currentPage")).isEqualTo(0);
        assertThat(found.get("totalItems")).isEqualTo(1L);
        assertThat(found.get("totalPages")).isEqualTo(1);
    }


    @Test
    public void testGetRiderInfoButNoRiders_thenReturn0Records() throws InvalidLoginException {
        List<Rider> riderList = new ArrayList<>();
        Page<Rider> pageRequest = new PageImpl(riderList, PageRequest.of(0, 10), riderList.size());

        Mockito.when(riderRepository.findAll(PageRequest.of(0, 10))).thenReturn(pageRequest);

        Map<String, Object> found = managerService.getRidersInformation(0, 10);

        Mockito.verify(riderRepository, VerificationModeFactory.times(1))
                .findAll(any(Pageable.class));

        assertThat(((List<Map<String, Object>>) found.get("riders"))).isEmpty();

        assertThat(found.get("currentPage")).isEqualTo(0);
        assertThat(found.get("totalItems")).isEqualTo(0L);
        assertThat(found.get("totalPages")).isEqualTo(0);
    }


    /* ----------------------------- *
     * GET ALL RIDERS STATS TESTS    *
     * ----------------------------- *
     */

    @Test
    public void testWhenGetRidersStatisticsButNoPurchasesHaveBeenDelivered_thenReturn() {
        List<Long[]> time = new LinkedList<>();
        time.add(new Long[]{null, 0L});

        Mockito.when(purchaseRepository.getSumDeliveryTimeAndCountPurchases()).thenReturn( time );
        Mockito.when(riderRepository.getAverageRiderRating()).thenReturn(null);
        Mockito.when(purchaseRepository.countPurchaseByStatusIsNot(Status.DELIVERED)).thenReturn(0L);

        Map<String, Object> found = managerService.getRidersStatistics();

        assertThat(found.get("avgReviews")).isEqualTo(null);
        assertThat(found.get("avgTimes")).isEqualTo(null);
        assertThat(found.get("inProcess")).isEqualTo(0L);

        Mockito.verify(purchaseRepository, times(1)).getSumDeliveryTimeAndCountPurchases();
        Mockito.verify(purchaseRepository, times(1)).countPurchaseByStatusIsNot(any());
        Mockito.verify(riderRepository, times(1)).getAverageRiderRating();

    }

    @Test
    public void testWhenGetRidersStatistics_thenReturn() {
        Address addr = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr_store = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store);

        Purchase p1 = new Purchase(addr, this.rider, store, "Miguel");
        Purchase p2 = new Purchase(addr, this.rider, store, "Mariana");
        Purchase p3 = new Purchase(addr, this.rider, store, "Carolina");

        p1.setStatus(Status.DELIVERED); p2.setStatus(Status.DELIVERED); p3.setStatus(Status.DELIVERED);
        p1.setDeliveryTime(264L); p2.setDeliveryTime(199L); p3.setDeliveryTime(230L);
        p1.getRider().setTotalNumReviews(2); p1.getRider().setReviewsSum(7);
        p2.getRider().setTotalNumReviews(1); p2.getRider().setReviewsSum(1);

        double exp_time = (double) (p1.getDeliveryTime() + p2.getDeliveryTime() + p3.getDeliveryTime()) / 3;
        double exp_rev = (double) (7/2 + 1)/2;

        List<Long[]> time = new LinkedList<>();
        time.add(new Long[]{p1.getDeliveryTime() + p2.getDeliveryTime() + p3.getDeliveryTime(), 3L});

        List<Long[]> reviews = new LinkedList<>();
        reviews.add(new Long[]{8L, 3L});

        Mockito.when(purchaseRepository.getSumDeliveryTimeAndCountPurchases()).thenReturn(time);
        Mockito.when(riderRepository.getAverageRiderRating()).thenReturn(exp_rev);
        Mockito.when(purchaseRepository.countPurchaseByStatusIsNot(Status.DELIVERED)).thenReturn(3L);

        Map<String, Object> found = managerService.getRidersStatistics();

        Mockito.verify(purchaseRepository, times(1)).getSumDeliveryTimeAndCountPurchases();
        Mockito.verify(purchaseRepository, times(1)).countPurchaseByStatusIsNot(any());
        Mockito.verify(riderRepository, times(1)).getAverageRiderRating();

        assertThat(found.get("avgTimes")).isEqualTo(exp_time);
        assertThat(found.get("avgReviews")).isEqualTo(exp_rev);
        assertThat(found.get("inProcess")).isEqualTo(3L);
    }
}
