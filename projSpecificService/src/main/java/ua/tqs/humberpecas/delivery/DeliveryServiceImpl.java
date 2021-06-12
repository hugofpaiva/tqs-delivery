package ua.tqs.humberpecas.delivery;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ua.tqs.humberpecas.model.Category;
import ua.tqs.humberpecas.model.Purchase;
import ua.tqs.humberpecas.model.Review;

@Component
public class DeliveryServiceImpl implements IDeliveryService {

    private final RestTemplate restTemplate = new RestTemplateBuilder().build();

    private final String serviceHost = "http://localhost:8081/";
    private String token;

    @Override
    public void connectDeliveryService() {

    }

    @Override
    public void newOrder(Purchase purchase) {

    }

    @Override
    public Category checkOrderStatus(int orderId) {
        return null;
    }

    @Override
    public void reviewRider(Review review) {

    }
}
