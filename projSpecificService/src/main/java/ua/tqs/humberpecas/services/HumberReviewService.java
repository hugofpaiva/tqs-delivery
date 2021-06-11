package ua.tqs.humberpecas.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.tqs.humberpecas.delivery.IDeliveryService;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.Review;

@Service
public class HumberReviewService {

    @Autowired
    private IDeliveryService deliveryService;

    public void addReview( Review review) throws ResourceNotFoundException {


        deliveryService.reviewRider(review);

        // enviar review pra o delivery service


    }

}
