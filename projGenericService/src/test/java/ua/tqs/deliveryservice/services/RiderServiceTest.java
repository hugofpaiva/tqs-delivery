package ua.tqs.deliveryservice.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.verification.VerificationModeFactory;

import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.repository.RiderRepository;

import static org.assertj.core.api.Assertions.assertThat;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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

        Rider response = riderService.save(rider);

        assertThat(response).isEqualTo(rider);

        Mockito.verify(riderRepository, VerificationModeFactory.times(1)).saveAndFlush(rider);
    }
}
