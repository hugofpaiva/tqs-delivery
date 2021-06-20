package ua.tqs.deliveryservice.specific;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ua.tqs.deliveryservice.exception.InvalidValueException;
import ua.tqs.deliveryservice.exception.UnreachableServiceException;
import ua.tqs.deliveryservice.handler.RestTemplateErrorHandler;
import ua.tqs.deliveryservice.model.Status;

import java.util.HashMap;
import java.util.Map;

@Component
public class SpecificServiceImpl implements ISpecificService{

    @Autowired
    private RestTemplate restTemplate;

    private HttpHeaders headers;
    private final static String TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDcwOTYwNDMsImlhdCI6MTYyMzA5OTI0MywiU3ViamVjdCI6Ikh1bWJlclBlY2FzIn0.oEZD63J134yUxHl658oSDJrw32BZcYHQbveZw8koAgP-2_d-8aH2wgJYJMlGnKIugOiI8H9Aa4OjPMWMUl9BFw";

    public SpecificServiceImpl(){
        this.headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + TOKEN);
    }

    @Override
    public void updateOrderStatus(Status orderStatus, String storeUrl) throws UnreachableServiceException, InvalidValueException {

        Map<String,Status> purchaseStatus = new HashMap<>();
        purchaseStatus.put("orderStatus", orderStatus);

        ResponseEntity<HttpStatus> response = restTemplate.exchange(
                storeUrl , HttpMethod.PUT, new HttpEntity<>(purchaseStatus, headers),
                HttpStatus.class);


    }

    @Override
    public void setRiderName(String rider, String storeUrl) throws UnreachableServiceException, InvalidValueException {

        System.out.println(storeUrl);

        Map<String,String> purchaseStatus = new HashMap<>();
        purchaseStatus.put("rider", rider);

        ResponseEntity<HttpStatus> response = restTemplate.exchange(
                storeUrl , HttpMethod.PUT, new HttpEntity<>(purchaseStatus, headers),
                HttpStatus.class);

    }

    @Bean
    public RestTemplate restTemplate(){
       return new RestTemplateBuilder().errorHandler(new RestTemplateErrorHandler()).build();
    }

}
