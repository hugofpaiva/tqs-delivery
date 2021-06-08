package ua.tqs.deliveryservice.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.tqs.deliveryservice.model.*;
import ua.tqs.deliveryservice.repository.PurchaseRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class PurchaseServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private PurchaseService purchaseService;

    @Test
    public void givenAvailableOrder_whenGetAvailableOrderForRider_thenGetThatOrder() {
        // setting up ...
        Address addr1 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr2 = new Address("Rua Loja Loja, n. 23", "3212-333", "Porto", "Portugal");
        Store store1 = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr2);
        Purchase available = new Purchase(addr1, store1, "João");

        Mockito
                .when( purchaseRepository.findTopByRiderIsNullOrderByDate() )
                .thenReturn( available );

        // test ...
        Purchase ret = purchaseService.getAvailableOrderForRider();
        assertThat(ret).isEqualTo(available);
        Mockito.verify(purchaseRepository, times(1)).findTopByRiderIsNullOrderByDate();
    }

    @Test
    public void givenNoAvailableOrder_whenGetAvailableOrderForRider_thenGetNull() {
        // setting up ...
        Mockito
                .when( purchaseRepository.findTopByRiderIsNullOrderByDate() )
                .thenReturn( null );

        // test ...
        Purchase ret = purchaseService.getAvailableOrderForRider();
        assertThat(ret).isNull();
        Mockito.verify(purchaseRepository, times(1)).findTopByRiderIsNullOrderByDate();
    }

    @Test
    public void givenRiderAndPurchase_whenAcceptOrder_thenPurchasedIsChanged() {
        // setting up ...
        Rider r1 = new Rider("MM", "pwd", "r1@email.com");

        Address addr1 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr2 = new Address("Rua Loja Loja, n. 23", "3212-333", "Porto", "Portugal");
        Store store1 = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr2);
        Purchase available = new Purchase(addr1, store1, "João");

        Mockito
                .when(purchaseRepository.save(available))
                .thenReturn(available);

        // test ...
        Purchase expectedRet = new Purchase(addr1, r1, store1, "João");
        expectedRet.setStatus(Status.ACCEPTED);

        Purchase ret = purchaseService.acceptOrder(r1, available);
        assertThat(ret).isEqualTo(expectedRet);
    }


    @Test
    public void givenRiderWithoutOrder_whenGetCurrentRiderOrder_returnNull() {
        // setting up ...
        Rider r1 = new Rider("MM", "pwd", "r1@email.com");

        Mockito
                .when( purchaseRepository.findTopByRiderAndStatusIsNot( r1, Status.DELIVERED) )
                .thenReturn(null);

        // test ...

        Purchase ret = purchaseService.getCurrentRiderOrder(r1);
        assertThat(ret).isNull();
        Mockito.verify( purchaseRepository, times(1) ).findTopByRiderAndStatusIsNot(r1, Status.DELIVERED);
    }

    @Test
    public void givenRiderWithOrder_whenGetCurrentRiderOrder_returnOrder() {
        // setting up ...
        Rider r1 = new Rider("MM", "pwd", "r1@email.com");
        Address addr1 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr2 = new Address("Rua Loja Loja, n. 23", "3212-333", "Porto", "Portugal");
        Store store1 = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr2);

        Purchase purch = new Purchase(addr1, r1, store1, "João");
        purch.setStatus(Status.ACCEPTED);

        Mockito
                .when( purchaseRepository.findTopByRiderAndStatusIsNot( r1, Status.DELIVERED) )
                .thenReturn(purch);

        // test ...
        Purchase ret = purchaseService.getCurrentRiderOrder(r1);
        assertThat(ret).isNotNull();
        assertThat(ret).isEqualTo(purch);
        Mockito.verify( purchaseRepository, times(1) ).findTopByRiderAndStatusIsNot(r1, Status.DELIVERED);
    }


}
