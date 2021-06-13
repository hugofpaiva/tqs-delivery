package ua.tqs.humberpecas.service;


import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.tqs.humberpecas.delivery.IDeliveryService;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.Review;
import ua.tqs.humberpecas.repository.PurchaseRepository;

@Log4j2
@Service
public class HumberReviewService {

    @Autowired
    private IDeliveryService deliveryService;

    @Autowired
    private PurchaseRepository repository;

    public void addReview( Review review) throws ResourceNotFoundException {

        repository.findById(review.getOrderId()).orElseThrow(() -> {
            log.error("Invalid Order");
            return new ResourceNotFoundException("Invalid Order");
        });

        deliveryService.reviewRider(review);

        // enviar review pra o delivery service

    }

}
