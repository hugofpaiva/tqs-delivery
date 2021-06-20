package ua.tqs.deliveryservice.services;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.verification.VerificationModeFactory;

import org.springframework.security.crypto.password.PasswordEncoder;


import ua.tqs.deliveryservice.exception.DuplicatedObjectException;

import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.repository.RiderRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;



@ExtendWith(MockitoExtension.class)
class RiderServiceTest {
    private Rider rider = new Rider("Nice Rider", "chunky_password", "email@TQS.ua");

    @Mock
    private RiderRepository riderRepository;

    @Mock
    private PasswordEncoder bcryptEncoder;

    @InjectMocks
    private RiderService riderService;


    @Mock
    private JwtUserDetailsService jwtUserDetailsService;

    /* ----------------------------- *
     * GET RIDER SAVE                *
     * ----------------------------- *
     */

    @Test
    public void testRiderSave_WhenRiderValid_thenReturnIt() throws DuplicatedObjectException {
        Mockito.when(riderRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        Mockito.when(riderRepository.saveAndFlush(rider)).thenReturn(rider);
        Mockito.when(bcryptEncoder.encode(rider.getPwd())).thenReturn(rider.getPwd());

        Rider response = riderService.save(rider);

        assertThat(response).isEqualTo(rider);

        Mockito.verify(riderRepository, VerificationModeFactory.times(1)).saveAndFlush(rider);
    }

    @Test
    public void testRiderSave_WhenEmailAlreadyExists_thenThrow() throws DuplicatedObjectException {
        Mockito.when(riderRepository.findByEmail(anyString())).thenReturn(Optional.of(rider));

        assertThrows(DuplicatedObjectException.class, () -> {
            riderService.save(rider);
        }, "Rider with this email already exists.");

        Mockito.verify(riderRepository, VerificationModeFactory.times(0)).saveAndFlush(rider);
    }


    /* ----------------------------- *
     * GET RIDER REVIEW STATISTICS   *
     * ----------------------------- *
     */

    @Test
    public void givenInvalidRider_whenGetRatingStatistics_throwException() {
        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.empty());

        assertThrows(InvalidLoginException.class, () -> {
            riderService.getRatingStatistics("exampleToken");
        }, "There is no Rider associated with this token");

        Mockito.verify(jwtUserDetailsService, times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(riderRepository, times(1))
                .findByEmail("email@email.com");
    }


    @Test
    public void givenRiderWithNoReviews_whenGetStatistics_thenReturnStatistics() throws InvalidLoginException {
        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.of(rider));

        Map<String, Object> found = riderService.getRatingStatistics("exampleToken");

        assertThat(found.get("totalNumReviews")).isEqualTo(0L);
        assertThat(found.get("avgReviews")).isNull();
    }

    @Test
    public void givenRiderWithReviews_whenGetStatistics_thenReturnStatistics() throws InvalidLoginException {
        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.of(rider));

        rider.setReviewsSum(15);
        rider.setTotalNumReviews(4);

        Map<String, Object> found = riderService.getRatingStatistics("exampleToken");

        assertThat(found.get("totalNumReviews")).isEqualTo(4L);
        assertThat(found.get("avgReviews")).isEqualTo((double) 15/4.0 );
    }

}
