package ua.tqs.deliveryservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.exception.InvalidValueException;
import ua.tqs.deliveryservice.exception.ResourceNotFoundException;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Store;
import ua.tqs.deliveryservice.repository.PurchaseRepository;
import ua.tqs.deliveryservice.repository.StoreRepository;

import java.util.Optional;


@Service
public class PurchaseService {
    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    public Purchase reviewRiderFromSpecificOrder(String storeToken, Long order_id, int review)
            throws InvalidLoginException, ResourceNotFoundException, InvalidValueException {
        // The store token that was passed did not match any in the db.
        if (storeRepository.findByToken(storeToken).isEmpty()) throw new InvalidLoginException("Unauthorized store.");
        Store store = storeRepository.findByToken(storeToken).get();

        // The order_id that was passed did not match any in the db.
        if (purchaseRepository.findById(order_id).isEmpty()) throw new ResourceNotFoundException("Order not found.");
        Purchase purchase = purchaseRepository.findById(order_id).get();

        // A review cannot be added to a purchase that was already reviewed.
        if (purchase.getRiderReview() != null) throw new InvalidValueException("Invalid, purchased already had review.");

        long store_id_of_where_purchase_was_supposedly_made = purchase.getStore().getId();
        long store_id_associated_to_token_passed = store.getId();

        // The token passed belonged to a store where this purchase had not been made,
        // because this purchase_id was not associated with the store in possession of the passed token
        if (store_id_of_where_purchase_was_supposedly_made != store_id_associated_to_token_passed)
            throw new InvalidValueException("Token passed belonged to a store where this purchase had not been made.");

        purchase.setRiderReview(review);
        purchaseRepository.saveAndFlush(purchase);

        return purchase;
    }
}
