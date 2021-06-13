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
    public void testGetRidersInfoButNoManagerFound_thenInvalidLogin() {
        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(managerRepository.findByEmail("email@email.com")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            managerService.getRidersInformation(0, 10, "exampleToken");
        });

        Mockito.verify(jwtUserDetailsService, times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(managerRepository, times(1))
                .findByEmail("email@email.com");
    }

    @Test
    public void testGetRidersButInvalidPageNo_thenThrow() {
        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(managerRepository.findByEmail("email@email.com")).thenReturn(Optional.of(this.manager));

        assertThrows(IllegalArgumentException.class, () -> {
            managerService.getRidersInformation(-1, 10, "exampleToken");
        });

        Mockito.verify(jwtUserDetailsService, times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(managerRepository, times(1))
                .findByEmail("email@email.com");
    }

    @Test
    public void testGetRidersButInvalidPageSize_thenThrow() {
        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(managerRepository.findByEmail("email@email.com")).thenReturn(Optional.of(this.manager));

        assertThrows(IllegalArgumentException.class, () -> {
            managerService.getRidersInformation(0, -1, "exampleToken");
        });

        Mockito.verify(jwtUserDetailsService, times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(managerRepository, times(1))
                .findByEmail("email@email.com");
    }

    @Test
    public void testGetRiderInfoEverythingValid_thenReturn1Record() throws InvalidLoginException {
        this.rider.setPurchases(Arrays.asList(this.purchase));
        this.rider.setReviewsSum(4);
        this.rider.setTotalNumReviews(1);

        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(managerRepository.findByEmail("email@email.com")).thenReturn(Optional.of(this.manager));

        List<Rider> riderList = Arrays.asList(this.rider);
        Page<Rider> pageRequest = new PageImpl(riderList, PageRequest.of(0, 10), riderList.size());

        Mockito.when(riderRepository.findAll(PageRequest.of(0, 10))).thenReturn(pageRequest);

        Map<String, Object> found = managerService.getRidersInformation(0, 10, "exampleToken");

        Mockito.verify(jwtUserDetailsService, VerificationModeFactory.times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(managerRepository, VerificationModeFactory.times(1))
                .findByEmail("email@email.com");
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

        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(managerRepository.findByEmail("email@email.com")).thenReturn(Optional.of(this.manager));

        List<Rider> riderList = new ArrayList<>();
        Page<Rider> pageRequest = new PageImpl(riderList, PageRequest.of(0, 10), riderList.size());

        Mockito.when(riderRepository.findAll(PageRequest.of(0, 10))).thenReturn(pageRequest);

        Map<String, Object> found = managerService.getRidersInformation(0, 10, "exampleToken");

        Mockito.verify(jwtUserDetailsService, VerificationModeFactory.times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(managerRepository, VerificationModeFactory.times(1))
                .findByEmail("email@email.com");
        Mockito.verify(riderRepository, VerificationModeFactory.times(1))
                .findAll(any(Pageable.class));

        assertThat(((List<Map<String, Object>>) found.get("riders"))).isEmpty();

        assertThat(found.get("currentPage")).isEqualTo(0);
        assertThat(found.get("totalItems")).isEqualTo(0L);
        assertThat(found.get("totalPages")).isEqualTo(0);
    }
}
