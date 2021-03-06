package ua.tqs.humberpecas.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.tqs.humberpecas.delivery.IDeliveryService;
import ua.tqs.humberpecas.exception.AccessNotAllowedException;
import ua.tqs.humberpecas.exception.InvalidOperationException;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.exception.UnreachableServiceException;
import ua.tqs.humberpecas.model.Purchase;
import ua.tqs.humberpecas.model.PurchaseStatus;
import ua.tqs.humberpecas.model.Review;
import ua.tqs.humberpecas.repository.PurchaseRepository;

@Log4j2
@Service
public class HumberReviewService {

    @Autowired
    private IDeliveryService deliveryService;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    public Purchase addReview(Review review, String userToken) throws ResourceNotFoundException, UnreachableServiceException, AccessNotAllowedException {


        var purchase = purchaseRepository.findById(review.getOrderId())
                .orElseThrow(() -> {
                    log.error("HUMBER REVIEW SERVICE: Invalid Purchase");
                    throw new ResourceNotFoundException("Invalid Purchase");
                });

        if (purchase.getStatus() != PurchaseStatus.DELIVERED){
            log.error("ReviewService: Purchase not delivered");
            throw new InvalidOperationException("Could not Review order");
        }

        String personEmail = purchase.getPerson().getEmail();

        if (!personEmail.equals(jwtUserDetailsService.getEmailFromToken(userToken))) {
            log.error("HUMBER REVIEW SERVICE: Invalid Purchase Access");
            throw new AccessNotAllowedException("Not Allowed");
        }

        review.setOrderId(purchase.getServiceOrderId());
        deliveryService.reviewRider(review);

        purchase.setRiderReview(review.getReview());

        log.info("HUMBER REVIEW SERVICE: Successfully added new review");
        return purchaseRepository.saveAndFlush(purchase);
    }

}
