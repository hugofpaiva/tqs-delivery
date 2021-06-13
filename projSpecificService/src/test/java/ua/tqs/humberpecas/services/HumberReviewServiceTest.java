package ua.tqs.humberpecas.services;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.tqs.humberpecas.delivery.IDeliveryService;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.*;
import ua.tqs.humberpecas.repository.PurchaseRepository;
import ua.tqs.humberpecas.service.HumberReviewService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class HumberReviewServiceTest {

    @Mock
    private PurchaseRepository repository;

    @Mock
    private IDeliveryService deliveryService;

    @InjectMocks
    private HumberReviewService service;

    private Purchase purchase;
    private Review review;

    @BeforeEach
    public void setUp(){

        ShoppingCart sc = new ShoppingCart();
        Person person = new Person("Fernando", "12345678","fernando@ua.pt");
        Address address  = new Address("Aveiro", "3730-123","Aveiro","Portugal", person);

        List<Product> products = new ArrayList<>();
        products.add(new Product("hammer", 10.50, Category.SCREWDRIVER , "the best hammer", 3));
        products.add(new Product("hammer v2", 20.50, Category.SCREWDRIVER , "the best hammer 2.0", 4));

        purchase = new Purchase(person, address, products);
        purchase.setId(1);

        review = new Review(1, 4);

    }

    @Test
    @DisplayName("Review Rider")
    void whenValidPurchage_thenSendReview() throws ResourceNotFoundException {

        when(repository.findById(Mockito.anyLong())).thenReturn(Optional.of(purchase));
        doNothing().when(deliveryService).reviewRider(review);

        service.addReview(review);

        verify(repository, times(1)).findById(1L);
        verify(deliveryService, times(1)).reviewRider(review);

    }


    @Test
    @DisplayName("Review Rider with invalid orderId throws ResourceNotFoundException")
    void whenInvalidPurchage_thenThrowsStatus404() throws ResourceNotFoundException {

        assertThrows( ResourceNotFoundException.class, () -> {
            service.addReview(review);
        } );

        verify(repository, times(1)).findById(1L);
        verify(deliveryService, times(0)).reviewRider(review);

    }


}
