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
import ua.tqs.deliveryservice.repository.StoreRepository;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private ManagerRepository managerRepository;

    @Mock
    private JwtUserDetailsService jwtUserDetailsService;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private StoreService storeService;


    /* ----------------------------- *
     * GET LAST STORES (FOR MANAGER) *
     * ----------------------------- *
     */


    @Test
    void testGetStoresWhenGetInvalidPageNo_thenThrow() {

        assertThrows(IllegalArgumentException.class, () -> {
            storeService.getStores(-1, 10);
        });
        Mockito.verify(storeRepository, VerificationModeFactory.times(0))
                .findAll(any(Pageable.class));
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(0))
                .countPurchaseByStore(any());
    }

    @Test
    void testGetStoresWhenGetInvalidPageSize_thenThrow() {

        assertThrows(IllegalArgumentException.class, () -> {
            storeService.getStores(0, -1);
        });

        Mockito.verify(storeRepository, VerificationModeFactory.times(0))
                .findAll(any(Pageable.class));
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(0))
                .countPurchaseByStore(any());
    }


    @Test
    void givenNoStores_whenGetStores_thenReturn0Records() throws InvalidLoginException {

        Page<Store> pageRequest = new PageImpl(new ArrayList<>(), PageRequest.of(0, 10), new ArrayList<>().size());
        Mockito.when(storeRepository.findAll(PageRequest.of(0, 10))).thenReturn(pageRequest);

        Map<String, Object> found = storeService.getStores(0, 10);

        Mockito.verify(storeRepository, VerificationModeFactory.times(1))
                .findAll(any(Pageable.class));
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(0))
                .countPurchaseByStore(any());

        assertThat(((List<Object>) found.get("stores"))).isEmpty();
        assertThat(found).containsEntry("currentPage", 0).containsEntry("totalItems", 0L).containsEntry("totalPages", 0);
    }

    @Test
    void given3Stores_whenGetStores_thenReturn3Records() throws InvalidLoginException {
        Address addr1 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr2 = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Address addr3 = new Address("Rua ABC, n. 944", "4444-555", "Aveiro", "Portugal");
        Store store1 = new Store("Loja do Manel11", "A melhor loja.", "exToken1", addr1, "http://localhost:8081/delivery/");
        Store store2 = new Store("Loja do Manel22", "A melhor loja.", "exToken2", addr2, "http://localhost:8082/delivery/");
        Store store3 = new Store("Loja do Manel33", "A melhor loja.", "exToken3", addr3, "http://localhost:8083/delivery/");


        List<Store> allStores = Arrays.asList(store1, store2, store3);
        Page<Store> pageRequest = new PageImpl(allStores, PageRequest.of(0, 10), allStores.size());

        Mockito.when(storeRepository.findAll(PageRequest.of(0, 10))).thenReturn(pageRequest);
        Mockito.when(purchaseRepository.countPurchaseByStore(any())).thenReturn(2L);

        Map<String, Object> found = storeService.getStores(0, 10);


        Mockito.verify(storeRepository, VerificationModeFactory.times(1))
                .findAll(any(Pageable.class));
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(allStores.size()))
                .countPurchaseByStore(any());

        assertThat( ((List<Map<String, Object>>) found.get("stores"))).hasSize(3)
            .extracting("name").contains(store1.getName(), store2.getName(), store3.getName());
        assertThat( ((List<Map<String, Object>>) found.get("stores")))
                .extracting("totalOrders").contains(2L, 2L, 2L);

        assertThat(found).containsEntry("currentPage", 0).containsEntry("totalItems", 3L).containsEntry("totalPages", 1);
    }
    /* ----------------------------- *
     * GET STORE STATISTICS          *
     * ----------------------------- *
     */


    @Test
    void givenNoPurchases_whenGetStatistics_thenReturnStatistics() {
        purchaseRepository.deleteAll();

        Mockito.when(purchaseRepository.count()).thenReturn((long) 0);
        Mockito.when(storeRepository.count()).thenReturn((long) 0);
        Mockito.when(purchaseRepository.findTopByOrderByDate()).thenReturn(Optional.empty());

        Map<String, Object> found = storeService.getStatistics();

        Mockito.verify(purchaseRepository, VerificationModeFactory.times(1))
                .count();
        Mockito.verify(storeRepository, VerificationModeFactory.times(1))
                .count();

        assertThat(found).containsEntry("totalPurchases", 0L).containsEntry("avgPurchasesPerWeek", 0.0)
                .containsEntry("totalStores", 0L);
    }

    @Test
    void givenPurchases_whenGetStatistics_thenReturnStatistics() throws InvalidLoginException {

        Purchase p1 = new Purchase();
        p1.setDate(new Date());

        Mockito.when(purchaseRepository.count()).thenReturn((long) 2);
        Mockito.when(storeRepository.count()).thenReturn((long) 1);
        Mockito.when(purchaseRepository.findTopByOrderByDate()).thenReturn(Optional.of(p1));

        Map<String, Object> found = storeService.getStatistics();


        Mockito.verify(purchaseRepository, VerificationModeFactory.times(1))
                .count();
        Mockito.verify(storeRepository, VerificationModeFactory.times(1))
                .count();

        assertThat(found).containsEntry("totalPurchases", 2L).containsEntry("totalStores", 1L);
        assertThat(found.get("avgPurchasesPerWeek")).isNotNull();
    }

    /* ----------------------------- *
     * AUXILIARY METHODS TESTS       *
     * ----------------------------- *
     */

    @Test
    void givenDateTwoWeekAgo_whenGetNoWeeksUntilNow_thenReturn2() {
        Instant instant = Instant.now();  // Current moment in UTC.
        ZonedDateTime zdtNow = instant.atZone(ZoneId.of("Africa/Tunis"));
        Date oneWeekAgo = Date.from(zdtNow.minusWeeks(2).toInstant());
        Double timePassed = storeService.getNoWeeksUntilNow(oneWeekAgo);
        assertThat(timePassed.intValue()).isEqualTo(2);
    }

}
