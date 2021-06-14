package ua.tqs.deliveryservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.exception.InvalidValueException;
import ua.tqs.deliveryservice.exception.ResourceNotFoundException;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Status;
import ua.tqs.deliveryservice.model.Store;
import ua.tqs.deliveryservice.repository.PurchaseRepository;
import ua.tqs.deliveryservice.repository.StoreRepository;

import ua.tqs.deliveryservice.exception.ForbiddenRequestException;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.repository.RiderRepository;

import java.util.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
public class PurchaseService {
    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private RiderRepository riderRepository;

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    public Purchase reviewRiderFromSpecificOrder(String storeToken, Long order_id, int review)
            throws InvalidLoginException, ResourceNotFoundException, InvalidValueException {
        // The store token that was passed did not match any in the db. UNAUTHORIZED
        Store store = storeRepository.findByToken(storeToken).orElseThrow(() -> new InvalidLoginException("Unauthorized store."));

        // The order_id that was passed did not match any in the db. NOT_FOUND
        Purchase purchase = purchaseRepository.findById(order_id).orElseThrow(() -> new ResourceNotFoundException("Order not found."));

        // A review cannot be added to a purchase that was already reviewed. BAD_REQUEST
        if (purchase.getRiderReview() != null)
            throw new InvalidValueException("Invalid, purchased already had review.");

        long store_id_of_where_purchase_was_supposedly_made = purchase.getStore().getId();
        long store_id_associated_to_token_passed = store.getId();

        // The token passed belonged to a store where this purchase had not been made,
        // because this purchase_id was not associated with the store in possession of the passed token. BAD_REQUEST
        if (store_id_of_where_purchase_was_supposedly_made != store_id_associated_to_token_passed)
            throw new InvalidValueException("Token passed belonged to a store where this purchase had not been made.");

        purchase.setRiderReview(review);
        purchaseRepository.saveAndFlush(purchase);

        return purchase;
    }

    public Purchase updatePurchaseStatus(String token) throws InvalidLoginException, ResourceNotFoundException {
        String email = jwtUserDetailsService.getEmailFromToken(token);
        Rider rider = riderRepository.findByEmail(email).orElseThrow(() -> new InvalidLoginException("There is no Rider associated with this token"));
        Purchase unfinished = purchaseRepository.findTopByRiderAndStatusIsNot(rider, Status.DELIVERED).orElseThrow(() -> new ResourceNotFoundException("This rider hasn't accepted an order yet"));

        unfinished.setStatus(Status.getNext(unfinished.getStatus()));

        if (unfinished.getStatus() == Status.DELIVERED) {
            Date now = new Date();
            unfinished.setDeliveryTime(now.getTime() - unfinished.getDate().getTime());
        }

        purchaseRepository.save(unfinished);
        return unfinished;
    }

    public Purchase getNewPurchase(String token) throws InvalidLoginException, ForbiddenRequestException, ResourceNotFoundException {
        String email = jwtUserDetailsService.getEmailFromToken(token);
        Rider rider = riderRepository.findByEmail(email).orElseThrow(() -> new InvalidLoginException("There is no Rider associated with this token"));

        // verify if Rider has any purchase to deliver
        if (purchaseRepository.findTopByRiderAndStatusIsNot(rider, Status.DELIVERED).isPresent()) {
            throw new ForbiddenRequestException("This rider still has an order to deliver");
        }

        // get available order for rider
        Purchase purch = purchaseRepository.findTopByRiderIsNullOrderByDate().orElseThrow(() -> new ResourceNotFoundException("There are no more orders available"));

        // accept order
        purch.setRider(rider);
        purch.setStatus(Status.ACCEPTED);
        purchaseRepository.save(purch);

        return purch;
    }

    public Purchase getCurrentPurchase(String token) throws InvalidLoginException, ResourceNotFoundException {
        String email = jwtUserDetailsService.getEmailFromToken(token);
        Rider rider = riderRepository.findByEmail(email).orElseThrow(() -> new InvalidLoginException("There is no Rider associated with this token"));
        return purchaseRepository.findTopByRiderAndStatusIsNot(rider, Status.DELIVERED).orElseThrow(() -> new ResourceNotFoundException("This rider hasn't accepted an order yet"));
    }

    public Map<String, Object> getLastOrderForRider(Integer pageNo, Integer pageSize, String riderToken) throws InvalidLoginException {
        String email = jwtUserDetailsService.getEmailFromToken(riderToken);

        Rider rider = riderRepository.findByEmail(email).orElseThrow(() -> new InvalidLoginException("There is no Rider associated with this token"));

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by("date").descending());

        Page<Purchase> pagedResult = purchaseRepository.findAllByRider(rider, paging);

        List<Purchase> responseList = new ArrayList<>();

        if (pagedResult.hasContent()) {
            responseList = pagedResult.getContent();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("orders", responseList);
        response.put("currentPage", pagedResult.getNumber());
        response.put("totalItems", pagedResult.getTotalElements());
        response.put("totalPages", pagedResult.getTotalPages());

        return response;
    }
}
