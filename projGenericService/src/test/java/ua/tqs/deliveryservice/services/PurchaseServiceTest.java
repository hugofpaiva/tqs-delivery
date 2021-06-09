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
import ua.tqs.deliveryservice.model.Address;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.model.Store;
import ua.tqs.deliveryservice.repository.PurchaseRepository;
import ua.tqs.deliveryservice.repository.RiderRepository;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {
    private Rider rider = new Rider("example", "pwd", "email@email.com");

    @Mock
    private JwtUserDetailsService jwtUserDetailsService;

    @Mock
    private RiderRepository riderRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private PurchaseService purchaseService;

    @Test
    public void testWhenGetWithInvalidUser_thenThrow() {
        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.empty());

        assertThrows(InvalidLoginException.class, () -> {
            purchaseService.getLastOrderForRider(0, 10, "exampleToken");
        }, "There is no Rider associated with this token");

        Mockito.verify(jwtUserDetailsService, VerificationModeFactory.times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(riderRepository, VerificationModeFactory.times(1))
                .findByEmail("email@email.com");
    }

    @Test
    public void testWhenGetInvalidPageNo_thenThrow() {
        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.of(this.rider));

        assertThrows(IllegalArgumentException.class, () -> {
            purchaseService.getLastOrderForRider(-1, 10, "exampleToken");
        });

        Mockito.verify(jwtUserDetailsService, VerificationModeFactory.times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(riderRepository, VerificationModeFactory.times(1))
                .findByEmail("email@email.com");
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(0))
                .findAllByRider(any(Rider.class), any(Pageable.class));
    }

    @Test
    public void testWhenGetInvalidPageSize_thenThrow() {
        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.of(this.rider));

        assertThrows(IllegalArgumentException.class, () -> {
            purchaseService.getLastOrderForRider(0, -1, "exampleToken");
        });

        Mockito.verify(jwtUserDetailsService, VerificationModeFactory.times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(riderRepository, VerificationModeFactory.times(1))
                .findByEmail("email@email.com");
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(0))
                .findAllByRider(any(Rider.class), any(Pageable.class));
    }

    @Test
    public void testGivenNoPurchases_whenGetPurchases_thenReturn0Records() throws InvalidLoginException {
        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.of(this.rider));

        Page<Purchase> pageRequest = new PageImpl(new ArrayList<>(), PageRequest.of(0, 10, Sort.by("date").descending()), new ArrayList<>().size());
        Mockito.when(purchaseRepository.findAllByRider(this.rider, PageRequest.of(0, 10, Sort.by("date").descending()))).thenReturn(pageRequest);

        Map<String, Object> found = purchaseService.getLastOrderForRider(0, 10, "exampleToken");
        Mockito.verify(jwtUserDetailsService, VerificationModeFactory.times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(riderRepository, VerificationModeFactory.times(1))
                .findByEmail("email@email.com");
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(1))
                .findAllByRider(any(Rider.class), any(Pageable.class));

        assertThat(((List<Purchase>) found.get("orders"))).isEmpty();
        assertThat(found.get("currentPage")).isEqualTo(0);
        assertThat(found.get("totalItems")).isEqualTo(0L);
        assertThat(found.get("totalPages")).isEqualTo(0);
    }

    @Test
    public void testGiven3Requests_whenGetRequests_thenReturn3Records() throws InvalidLoginException {
        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.of(this.rider));

        Address addr = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr_store = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store);
        Purchase p1 = new Purchase(addr, this.rider, store, "Miguel");
        Purchase p2 = new Purchase(addr, this.rider, store, "Mariana");
        Purchase p3 = new Purchase(addr, this.rider, store, "Carolina");

        List<Purchase> allPurchases = Arrays.asList(p1, p2, p3);
        Page<Purchase> pageRequest = new PageImpl(allPurchases, PageRequest.of(0, 10, Sort.by("date").descending()), allPurchases.size());

        Mockito.when(purchaseRepository.findAllByRider(this.rider, PageRequest.of(0, 10, Sort.by("date").descending()))).thenReturn(pageRequest);

        Map<String, Object> found = purchaseService.getLastOrderForRider(0, 10, "exampleToken");

        Mockito.verify(jwtUserDetailsService, VerificationModeFactory.times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(riderRepository, VerificationModeFactory.times(1))
                .findByEmail("email@email.com");
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(1))
                .findAllByRider(any(Rider.class), any(Pageable.class));

        assertThat(((List<Purchase>) found.get("orders"))).hasSize(3).extracting(Purchase::getClientName).contains(p1.getClientName(), p2.getClientName(),
                p3.getClientName());

        assertThat(found.get("currentPage")).isEqualTo(0);
        assertThat(found.get("totalItems")).isEqualTo(3L);
        assertThat(found.get("totalPages")).isEqualTo(1);
    }

}