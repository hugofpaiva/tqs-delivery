package ua.tqs.humberpecas.delivery;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ua.tqs.humberpecas.dto.PurchaseDeliveryDTO;
import ua.tqs.humberpecas.dto.ServerPurchaseDTO;
import ua.tqs.humberpecas.dto.ServerReviewDTO;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.handler.RestTemplateErrorHandler;
import ua.tqs.humberpecas.model.Category;
import ua.tqs.humberpecas.model.Review;

import java.util.Objects;

@Log4j2
@Component
public class DeliveryServiceImpl implements IDeliveryService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String HOST = "http://localhost:8081/store";
    private static final String TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g";

    private HttpHeaders headers;

    public DeliveryServiceImpl() {

        this.headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + TOKEN);
    }

    @Override
    public Long newOrder(PurchaseDeliveryDTO purchase) {

        StringBuilder url = new StringBuilder().append(HOST)
                .append("/order");

        ResponseEntity<ServerPurchaseDTO> response = restTemplate.exchange(
                url.toString(), HttpMethod.POST, new HttpEntity<>(purchase, headers),
                ServerPurchaseDTO.class);
        try {

            return Objects.requireNonNull(response.getBody()).getOrderId();

        } catch (NullPointerException e){

            log.error("DeliveryServiceImpl: Null serverOrderID ");
            throw new ResourceNotFoundException("Null serverOrderID");

        }

    }

    @Override
    public Category checkOrderStatus(int orderId) {
        return null;
    }

    @Override
    public String reviewRider(Review review) throws ResourceNotFoundException {

        StringBuilder url  = new StringBuilder().append(HOST)
                .append("/order/")
                .append( review.getOrderId())
                .append("/review");


        ResponseEntity<ServerReviewDTO> response = restTemplate.exchange(
                url.toString(), HttpMethod.PATCH, new HttpEntity<>(review, headers),
                ServerReviewDTO.class);


        try {

            return Objects.requireNonNull(response.getBody()).getRider();

        } catch (NullPointerException e){

            log.error("DeliveryServiceImpl: Null riderName ");
            throw new ResourceNotFoundException("Null riderName");

        }
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder().errorHandler(new RestTemplateErrorHandler()).build();
    }
}


