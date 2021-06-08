package ua.tqs.deliveryservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.exception.InvalidValueException;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.repository.PurchaseRepository;
import ua.tqs.deliveryservice.repository.StoreRepository;

import java.util.Map;
import java.util.Optional;

@Service
public class PurchaseService {
    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    public ResponseEntity<Map<String, Long>> reviewRiderFromSpecificOrder(String storeToken, Long order_id, Long review) throws InvalidValueException, InvalidLoginException {
        if (storeRepository.findByToken(storeToken).isEmpty()) throw new InvalidLoginException("Unauthorized store.");

        Optional<Purchase> requested_purchase = purchaseRepository.findById(order_id);
        if (requested_purchase.isEmpty()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Purchase purchase = requested_purchase.get();
        if (purchase.getRiderReview() != null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        long store_id_of_where_purchase_was_supposedly_made = purchase.getStore().getId();
        long store_id_associated_to_token_passed = storeRepository.findByToken(storeToken).get().getId();

        if (store_id_of_where_purchase_was_supposedly_made != store_id_associated_to_token_passed) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
