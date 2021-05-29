package ua.tqs.humberpecas.service;


import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.tqs.humberpecas.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
public class HumberServiceTest {


    @Mock( lenient = true)
    ProductRepository productRepository;

    @InjectMocks
    private HumberService service;


}
