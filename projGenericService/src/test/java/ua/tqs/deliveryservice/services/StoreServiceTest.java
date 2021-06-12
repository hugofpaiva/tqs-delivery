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

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class StoreServiceTest {

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
    public void testGetStores_whenInvalidUser() {
        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(managerRepository.findByEmail("email@email.com")).thenReturn(Optional.empty());

        assertThrows(InvalidLoginException.class, () -> {
            storeService.getStores(0, 10, "exampleToken");
        }, "There is no manager associated with this token");

        Mockito.verify(jwtUserDetailsService, times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(managerRepository, times(1))
                .findByEmail("email@email.com");
    }

    @Test
    public void testGetStoresWhenGetInvalidPageNo_thenThrow() {
        Manager manager = new Manager("man1", "pwd", "email@email.com");

        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(managerRepository.findByEmail("email@email.com")).thenReturn(Optional.of(manager));

        assertThrows(IllegalArgumentException.class, () -> {
            storeService.getStores(-1, 10, "exampleToken");
        });

        Mockito.verify(jwtUserDetailsService, VerificationModeFactory.times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(managerRepository, VerificationModeFactory.times(1))
                .findByEmail("email@email.com");
        Mockito.verify(storeRepository, VerificationModeFactory.times(0))
                .findAll(any(Pageable.class));
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(0))
                .countPurchaseByStore(any());
    }

    @Test
    public void testGetStoresWhenGetInvalidPageSize_thenThrow() {
        Manager manager = new Manager("man1", "pwd", "email@email.com");

        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(managerRepository.findByEmail("email@email.com")).thenReturn(Optional.of(manager));

        assertThrows(IllegalArgumentException.class, () -> {
            storeService.getStores(0, -1, "exampleToken");
        });

        Mockito.verify(jwtUserDetailsService, VerificationModeFactory.times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(managerRepository, VerificationModeFactory.times(1))
                .findByEmail("email@email.com");
        Mockito.verify(storeRepository, VerificationModeFactory.times(0))
                .findAll(any(Pageable.class));
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(0))
                .countPurchaseByStore(any());
    }


    @Test
    public void givenNoStores_whenGetStores_thenReturn0Records() throws InvalidLoginException {
        Manager manager = new Manager("man1", "pwd", "email@email.com");

        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(managerRepository.findByEmail("email@email.com")).thenReturn(Optional.of(manager));

        Page<Store> pageRequest = new PageImpl(new ArrayList<>(), PageRequest.of(0, 10), new ArrayList<>().size());
        Mockito.when(storeRepository.findAll(PageRequest.of(0, 10))).thenReturn(pageRequest);

        Map<String, Object> found = storeService.getStores(0, 10, "exampleToken");

        Mockito.verify(jwtUserDetailsService, VerificationModeFactory.times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(managerRepository, VerificationModeFactory.times(1))
                .findByEmail("email@email.com");
        Mockito.verify(storeRepository, VerificationModeFactory.times(1))
                .findAll(any(Pageable.class));
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(0))
                .countPurchaseByStore(any());

        assertThat(((List<Object>) found.get("stores"))).isEmpty();
        assertThat(found.get("currentPage")).isEqualTo(0);
        assertThat(found.get("totalItems")).isEqualTo(0L);
        assertThat(found.get("totalPages")).isEqualTo(0);
    }

    @Test
    public void given3Stores_whenGetStores_thenReturn3Records() throws InvalidLoginException {
        Manager manager = new Manager("man1", "pwd", "email@email.com");

        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(managerRepository.findByEmail("email@email.com")).thenReturn(Optional.of(manager));

        Address addr1 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr2 = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Address addr3 = new Address("Rua ABC, n. 944", "4444-555", "Aveiro", "Portugal");
        Store store1 = new Store("Loja do Manel11", "A melhor loja.", "exToken1", addr1);
        Store store2 = new Store("Loja do Manel22", "A melhor loja.", "exToken2", addr2);
        Store store3 = new Store("Loja do Manel33", "A melhor loja.", "exToken3", addr3);


        List<Store> allStores = Arrays.asList(store1, store2, store3);
        Page<Store> pageRequest = new PageImpl(allStores, PageRequest.of(0, 10), allStores.size());

        Mockito.when(storeRepository.findAll(PageRequest.of(0, 10))).thenReturn(pageRequest);
        Mockito.when(purchaseRepository.countPurchaseByStore(any())).thenReturn(2L);

        Map<String, Object> found = storeService.getStores(0, 10, "exampleToken");

        Mockito.verify(jwtUserDetailsService, VerificationModeFactory.times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(managerRepository, VerificationModeFactory.times(1))
                .findByEmail("email@email.com");

        Mockito.verify(storeRepository, VerificationModeFactory.times(1))
                .findAll(any(Pageable.class));
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(allStores.size()))
                .countPurchaseByStore(any());

        assertThat( ((List<Map<String, Object>>) found.get("stores"))).hasSize(3)
            .extracting("name").contains(store1.getName(), store2.getName(), store3.getName());
        assertThat( ((List<Map<String, Object>>) found.get("stores")))
                .extracting("totalOrders").contains(2L, 2L, 2L);

        assertThat(found.get("currentPage")).isEqualTo(0);
        assertThat(found.get("totalItems")).isEqualTo(3L);
        assertThat(found.get("totalPages")).isEqualTo(1);
    }
    /* ----------------------------- *
     * GET STORE STATISTICS          *
     * ----------------------------- *
     */

    @Test
    public void testGetStatistics_whenInvalidUser() {
        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(managerRepository.findByEmail("email@email.com")).thenReturn(Optional.empty());

        assertThrows(InvalidLoginException.class, () -> {
            storeService.getStatistics("exampleToken");
        }, "There is no manager associated with this token");

        Mockito.verify(jwtUserDetailsService, times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(managerRepository, times(1))
                .findByEmail("email@email.com");
    }


    @Test
    public void givenNoPurchases_whenGetStatistics_thenReturnStatistics() throws InvalidLoginException {
        Manager manager = new Manager("man1", "pwd", "email@email.com");
        purchaseRepository.deleteAll();

        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(managerRepository.findByEmail("email@email.com")).thenReturn(Optional.of(manager));

        Mockito.when(purchaseRepository.count()).thenReturn((long) 0);
        Mockito.when(storeRepository.count()).thenReturn((long) 0);
        Mockito.when(purchaseRepository.findTopByOrderByDate()).thenReturn(Optional.empty());

        Map<String, Object> found = storeService.getStatistics("exampleToken");

        Mockito.verify(jwtUserDetailsService, VerificationModeFactory.times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(managerRepository, VerificationModeFactory.times(1))
                .findByEmail("email@email.com");
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(1))
                .count();
        Mockito.verify(storeRepository, VerificationModeFactory.times(1))
                .count();

        assertThat(found.get("totalPurchases")).isEqualTo(0L);
        assertThat(found.get("avgPurchasesPerWeek")).isNull();
        assertThat(found.get("totalStores")).isEqualTo(0L);
    }

    @Test
    public void givenPurchases_whenGetStatistics_thenReturnStatistics() throws InvalidLoginException {
        Manager manager = new Manager("man1", "pwd", "email@email.com");

        Purchase p1 = new Purchase();
        p1.setDate(new Date());

        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(managerRepository.findByEmail("email@email.com")).thenReturn(Optional.of(manager));

        Mockito.when(purchaseRepository.count()).thenReturn((long) 2);
        Mockito.when(storeRepository.count()).thenReturn((long) 1);
        Mockito.when(purchaseRepository.findTopByOrderByDate()).thenReturn(Optional.of(p1));

        Map<String, Object> found = storeService.getStatistics("exampleToken");

        Mockito.verify(jwtUserDetailsService, VerificationModeFactory.times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(managerRepository, VerificationModeFactory.times(1))
                .findByEmail("email@email.com");
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(1))
                .count();
        Mockito.verify(storeRepository, VerificationModeFactory.times(1))
                .count();

        assertThat(found.get("totalPurchases")).isEqualTo(2L);
        assertThat(found.get("avgPurchasesPerWeek")).isNotNull();
        assertThat(found.get("totalStores")).isEqualTo(1L);
    }

}
