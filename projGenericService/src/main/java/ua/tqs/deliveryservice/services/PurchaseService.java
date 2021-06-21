package ua.tqs.deliveryservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.exception.InvalidValueException;
import ua.tqs.deliveryservice.exception.ResourceNotFoundException;
import ua.tqs.deliveryservice.model.*;
import ua.tqs.deliveryservice.repository.AddressRepository;
import ua.tqs.deliveryservice.repository.PurchaseRepository;
import ua.tqs.deliveryservice.repository.StoreRepository;

import ua.tqs.deliveryservice.exception.ForbiddenRequestException;
import ua.tqs.deliveryservice.repository.RiderRepository;

import java.util.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ua.tqs.deliveryservice.specific.ISpecificService;

@Service
public class PurchaseService {

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private ISpecificService specificService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private RiderRepository riderRepository;

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private AddressRepository addressRepository;


    public Purchase reviewRiderFromSpecificOrder(String storeToken, Long order_id, int review)
            throws InvalidLoginException, ResourceNotFoundException, InvalidValueException {
        // The store token that was passed did not match any in the db. UNAUTHORIZED

        Store store = storeRepository.findByToken(storeToken).orElseThrow(() -> new InvalidLoginException("Unauthorized store."));


        // The order_id that was passed did not match any in the db. NOT_FOUND
        Purchase purchase = purchaseRepository.findById(order_id).orElseThrow(() -> new ResourceNotFoundException("Order not found."));



        // A review cannot be added to a purchase that was already reviewed. BAD_REQUEST
        if (purchase.getRiderReview() != null)
            throw new InvalidValueException("Invalid, purchased already had review.");

        // A review cannot be added to a purchase when it is not delivered. BAD_REQUEST
        if (purchase.getStatus() != Status.DELIVERED)
            throw new InvalidValueException("Invalid, purchase must be delivered first.");

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

        Store store = unfinished.getStore();

        StringBuilder url  = new StringBuilder().append(store.getStoreUrl())
                .append("updateStatus?serverOrderId=")
                .append( unfinished.getId());


        specificService.updateOrderStatus(unfinished.getStatus(), url.toString());

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

        Store store = purch.getStore();

        StringBuilder url  = new StringBuilder().append(store.getStoreUrl())
                .append("setRider?serverOrderId=")
                .append( purch.getId());

        specificService.setRiderName(rider.getName(), url.toString());

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


    public Purchase receiveNewOrder(String storeToken, Map<String, Object> data) throws InvalidValueException, InvalidLoginException {
        Store store = jwtUserDetailsService.getStoreFromToken(storeToken);
        if (store == null) throw new InvalidLoginException("There is no Store associated with this token");

        String error = "invalid data";

        Object personName = Optional.ofNullable(data.get("personName"))
                .orElseThrow(() -> new InvalidValueException(error));

        if (!(personName instanceof String)) throw new InvalidValueException(error);

        Object address = Optional.ofNullable(data.get("address"))
                .orElseThrow(() -> new InvalidValueException(error));

        Address addr = new Address();
        try {
            addr.setAddress(((Map<String, String>) address).get("address"));
            addr.setCity(((Map<String, String>) address).get("city"));
            addr.setCountry(((Map<String, String>) address).get("country"));
            addr.setPostalCode(((Map<String, String>) address).get("postalCode"));
        } catch (Exception ex) {
            throw new InvalidValueException(error);
        }

        addressRepository.save(addr);
        Purchase purchase = new Purchase(addr, store, (String) personName);
        purchaseRepository.save(purchase);
        return purchase;

    }


    public Purchase getNewPurchaseLoc(String token, Double latitude, Double longitude) throws InvalidLoginException, ForbiddenRequestException, ResourceNotFoundException, InvalidValueException {
        String email = jwtUserDetailsService.getEmailFromToken(token);
        Rider rider = riderRepository.findByEmail(email).orElseThrow(() -> new InvalidLoginException("There is no Rider associated with this token"));

        if (latitude > 90 || latitude < -90 || longitude > 180 || longitude < -180) throw new InvalidValueException("Invalid values for coordinates");

            // verify if Rider has any purchase to deliver
        if (purchaseRepository.findTopByRiderAndStatusIsNot(rider, Status.DELIVERED).isPresent()) {
            throw new ForbiddenRequestException("This rider still has an order to deliver");
        }

        // get available order for rider
        Pageable paging = PageRequest.of(0, 15, Sort.by("date").ascending());
        Page<Purchase> possible = purchaseRepository.findAllByRiderIsNullOrderByDate(paging);
        Purchase purch = possible.stream().min(Comparator.comparingDouble(purchase -> distance(purchase.getStore(), latitude, longitude))).orElseThrow(() -> new ResourceNotFoundException("There are no more orders available"));

        // accept order
        purch.setRider(rider);
        purch.setStatus(Status.ACCEPTED);

        Store store = purch.getStore();

        System.out.println(store.getStoreUrl());

        StringBuilder url  = new StringBuilder().append(store.getStoreUrl())
                .append("setRider?serverOrderId=")
                .append( purch.getId());

        specificService.setRiderName(rider.getName(), url.toString());


        purchaseRepository.save(purch);

        return purch;
    }

    public static double distance(Store store, double x2, double y2) {
        return Math.sqrt((y2 - store.getLongitude()) * (y2 - store.getLongitude()) + (x2 - store.getLatitude()) * (x2 - store.getLatitude()));
    }

}

