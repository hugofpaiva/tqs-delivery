package ua.tqs.humberpecas.services;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.tqs.humberpecas.delivery.IDeliveryService;
import ua.tqs.humberpecas.exception.AccessNotAllowedException;
import ua.tqs.humberpecas.exception.InvalidOperationException;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.exception.UnreachableServiceException;
import ua.tqs.humberpecas.model.*;
import ua.tqs.humberpecas.repository.PurchaseRepository;
import ua.tqs.humberpecas.service.HumberReviewService;
import ua.tqs.humberpecas.service.JwtUserDetailsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class HumberReviewServiceTest {

    @Mock
    private IDeliveryService deliveryService;

    @Mock
    private JwtUserDetailsService jwtUserDetailsService;

    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private HumberReviewService service;

    private Purchase purchase;
    private Review review;
    private String userToken;
    private Person person;

    @BeforeEach
    public void setUp(){

        person = new Person("Fernando", "12345678","fernando@ua.pt");
        Address address  = new Address("Aveiro", "3730-123","Aveiro","Portugal", person);

        List<Product> products = new ArrayList<>();
        products.add(new Product("hammer", 10.50, Category.SCREWDRIVER , "the best hammer", "url"));
        products.add(new Product("hammer v2", 20.50, Category.SCREWDRIVER , "the best hammer 2.0", "url"));

        purchase = new Purchase(person, address, products);
        purchase.setId(1);
        purchase.setStatus(PurchaseStatus.DELIVERED);
        purchase.setServiceOrderId( 4L);


        review = new Review(1L, 4);

        this.userToken = "eyJhbGciOiJIUzI1NiJ9.eyJSb2xlIjoiQWRtaW4iLCJJc3N1ZXIiOiJJc3N1ZXIiLCJVc2VybmFtZSI6IkphdmFJblVzZSIsImV4cCI6MTYyMzYyMDQzMiwiaWF0IjoxNjIzNjIwNDMyfQ.Gib-gCJyL8-__G3zN4E-9VV1q75eYHZ8X6sS1WUNZB8";

    }

    @Test
    @DisplayName("Review Rider")
    void whenValidPurchage_thenSendReview() throws ResourceNotFoundException, AccessNotAllowedException {

        when(purchaseRepository.findById(anyLong())).thenReturn(Optional.of(purchase));
        when(jwtUserDetailsService.getEmailFromToken(anyString())).thenReturn(person.getEmail());
        when(purchaseRepository.saveAndFlush(any())).thenReturn(purchase);

        Purchase p = service.addReview(review, userToken);

        assertThat(p.getRiderReview(), Matchers.equalTo(4));
        assertThat(p.getPerson(), Matchers.equalTo(person));
        assertThat(p.getId(), Matchers.equalTo(purchase.getId()));

        verify(deliveryService, times(1)).reviewRider(review);
        verify(purchaseRepository, times(1)).findById(purchase.getId());
        verify(jwtUserDetailsService, times(1)).getEmailFromToken(userToken);
        verify(purchaseRepository, times(1)).saveAndFlush(purchase);
    }



    @Test
    @DisplayName("Review Rider When Review not deliverd throws InvalidOperationException")
    void whenReviewNotDeliveredOrder_thenThrowsInvalidOperation(){

        purchase.setStatus(PurchaseStatus.PENDENT);

        when(purchaseRepository.findById(anyLong())).thenReturn(Optional.of(purchase));

        assertThrows( InvalidOperationException.class, () -> {
            service.addReview(review, userToken);
        } );

        verify(deliveryService, times(0)).reviewRider(review);
        verify(purchaseRepository, times(1)).findById(purchase.getId());
        verify(jwtUserDetailsService, times(0)).getEmailFromToken(userToken);
        verify(purchaseRepository, times(0)).saveAndFlush(purchase);


    }



    @Test
    @DisplayName("Review Rider with invalid order in Delivery Service throws ResourceNotFoundException")
    void whenInvalidOrderService_thenThrowsStatusResourceNotFound(){

        when(purchaseRepository.findById(anyLong())).thenReturn(Optional.of(purchase));
        when(jwtUserDetailsService.getEmailFromToken(anyString())).thenReturn(person.getEmail());
        doThrow(ResourceNotFoundException.class).when(deliveryService).reviewRider(review);

        assertThrows( ResourceNotFoundException.class, () -> {
            service.addReview(review, userToken);
        } );

        verify(deliveryService, times(1)).reviewRider(review);
        verify(purchaseRepository, times(1)).findById(purchase.getId());
        verify(jwtUserDetailsService, times(1)).getEmailFromToken(userToken);

    }

    @Test
    @DisplayName("Review Rider with invalid order throws ResourceNotFoundException")
    void whenInvalidOrder_thenThrowsStatusResourceNotFound(){

        assertThrows( ResourceNotFoundException.class, () -> {
            service.addReview(review, userToken);
        } );

        verify(deliveryService, times(0)).reviewRider(review);
        verify(purchaseRepository, times(1)).findById(purchase.getId());
        verify(jwtUserDetailsService, times(0)).getEmailFromToken(userToken);

    }


    @Test
    @DisplayName("Cant communicate with delivery service throws UnreachableServiceExcption")
    void whenErrorInCommunication_thenThrowsStatusUnreachableService(){

        when(purchaseRepository.findById(anyLong())).thenReturn(Optional.of(purchase));
        when(jwtUserDetailsService.getEmailFromToken(anyString())).thenReturn(person.getEmail());
        doThrow(UnreachableServiceException.class).when(deliveryService).reviewRider(review);

        assertThrows( UnreachableServiceException.class, () -> {
            service.addReview(review, userToken);
        } );

        verify(deliveryService, times(1)).reviewRider(review);
        verify(purchaseRepository, times(1)).findById(purchase.getId());
        verify(jwtUserDetailsService, times(1)).getEmailFromToken(userToken);


    }

    @Test
    @DisplayName("User id not corresponds to purchase Owner throws AccessNotAllowedException")
    void whenUserNotCorrespondsOwner_thenthenReturnStatus405() throws AccessNotAllowedException {

        when(purchaseRepository.findById(anyLong())).thenReturn(Optional.of(purchase));
        when(jwtUserDetailsService.getEmailFromToken(anyString())).thenReturn("test@ua.pt");

        assertThrows( AccessNotAllowedException.class, () -> {
            service.addReview(review, userToken);
        } );

        verify(deliveryService, times(0)).reviewRider(review);
        verify(purchaseRepository, times(1)).findById(purchase.getId());
        verify(jwtUserDetailsService, times(1)).getEmailFromToken(userToken);

    }


}
