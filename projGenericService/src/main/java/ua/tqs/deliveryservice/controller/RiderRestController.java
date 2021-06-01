package ua.tqs.deliveryservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.model.Status;
import ua.tqs.deliveryservice.repository.PurchaseRepository;
import ua.tqs.deliveryservice.repository.RiderRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/rider")
public class RiderRestController {

    @Autowired
    private RiderRepository riderRep;

    @Autowired
    private PurchaseRepository purchaseRep;

    @PutMapping("/status/{newStatus}")
    public ResponseEntity<HttpStatus> updateOrderStatus(
            // @CurrentSecurityContext(expression="authentication.id") long rider_id, // not sure about this
            @PathVariable String newStatus
    ) {
        long rider_id = 1;  // TODO: check o id quando tiver seguranca

        // gets and verifies if rider is in the DB (should always happen if authenticated and rider...)
        Optional<Rider> riderOptional = riderRep.findById(rider_id);
        if (riderOptional.isEmpty()) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        Rider rider = riderOptional.get();

        // get the last purchase assigned to rider
        List<Purchase> purchases = rider.getPurchases();
        Purchase purchase = purchases.get(purchases.size() -1);

        // checks if 'newStatus' is valid and updates the statue of the last purchase/order
        Status status = Status.getEnumByString(newStatus);
        if (status == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        purchase.setStatus(status);
        purchaseRep.save(purchase);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/review")
    public ResponseEntity<HttpStatus> addReviewToRider(@RequestParam long order, @RequestParam int review_value) {
        long rider_id = 1;  // TODO: check o id quando tiver seguranca

        Optional<Rider> riderOptional = riderRep.findById(rider_id);
        if (riderOptional.isEmpty()) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        Rider rider = riderOptional.get();

        List<Purchase> purchases = rider.getPurchases();
        for (Purchase p : purchases) {
            if (p.getId() == order) {
                if (review_value >= 0 && review_value <= 5) {
                    p.setRiderReview(review_value);
                    purchaseRep.save(p);

                    return new ResponseEntity<>(HttpStatus.OK);
                }
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

}
