package ua.tqs.humberpecas.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.tqs.humberpecas.delivery.IDeliveryService;
import ua.tqs.humberpecas.repository.PurchaseRepository;


@ExtendWith(MockitoExtension.class)
class HumberServiceDeliveryTest {


    @Mock
   IDeliveryService deliveryService;

    @Mock( lenient = true)
    PurchaseRepository repository;

    @InjectMocks
    private HumberService service;

}