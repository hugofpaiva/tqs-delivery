package ua.tqs.deliveryservice.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.verification.VerificationModeFactory;

import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.exception.InvalidValueException;
import ua.tqs.deliveryservice.exception.ResourceNotFoundException;
import ua.tqs.deliveryservice.model.Address;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.model.Store;
import ua.tqs.deliveryservice.repository.PurchaseRepository;
import ua.tqs.deliveryservice.repository.RiderRepository;
import ua.tqs.deliveryservice.repository.StoreRepository;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.*;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class RiderServiceTest {
    private Rider rider = new Rider("Nice Rider", "chunky_password", "email@TQS.ua");
    @Mock
    private RiderRepository riderRepository;

    @InjectMocks
    private RiderService riderService;

    @Test
    public void testWhenRiderIsSent_thenReturnIt() {
        Mockito.when(riderRepository.saveAndFlush(rider)).thenReturn(rider);

        Mockito.verify(riderRepository, VerificationModeFactory.times(1)).saveAndFlush(rider);
        Rider response = riderService.save(rider);

        assertThat(response).isEqualTo(rider);
    }
}
