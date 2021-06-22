package ua.tqs.deliveryservice.services;

import lombok.extern.log4j.Log4j2;
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
@Log4j2
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

    public Purchase reviewRiderFromSpecificOrder(String storeToken, Long orderId, int review)
            throws InvalidLoginException, ResourceNotFoundException, InvalidValueException {
        // The store token that was passed did not match any in the db. UNAUTHORIZED
        Store store = storeRepository.findByToken(storeToken).orElseThrow(() -> {
            log.error("PURCHASE SERVICE: Unauthorized store, when reviewing rider");
            return new InvalidLoginException("Unauthorized store.");
        });


        // The order_id that was passed did not match any in the db. NOT_FOUND
        Purchase purchase = purchaseRepository.findById(orderId).orElseThrow(() -> {
            log.error("PURCHASE SERVICE: Order not found, when reviewing rider");
            return new ResourceNotFoundException("Order not found.");
        });



        // A review cannot be added to a purchase that was already reviewed. BAD_REQUEST
        if (purchase.getRiderReview() != null) {
            log.error("PURCHASE SERVICE: Purchase already had review, when reviewing rider");
            throw new InvalidValueException("Invalid, purchase already had review.");
        }

        // A review cannot be added to a purchase when it is not delivered. BAD_REQUEST
        if (purchase.getStatus() != Status.DELIVERED)
            throw new InvalidValueException("Invalid, purchase must be delivered first.");

        long storeIdOfWherePurchaseWasSupposedlyMade = purchase.getStore().getId();
        long storeIdAssociatedToTokenPassed = store.getId();

        // The token passed belonged to a store where this purchase had not been made,
        // because this purchase_id was not associated with the store in possession of the passed token. BAD_REQUEST
        if (storeIdOfWherePurchaseWasSupposedlyMade != storeIdAssociatedToTokenPassed) {
            log.error("PURCHASE SERVICE: Invalid token for purchase ID, when reviewing rider");
            throw new InvalidValueException("Token passed belonged to a store where this purchase had not been made.");
        }

        purchase.setRiderReview(review);
        purchaseRepository.saveAndFlush(purchase);

        Rider rider = purchase.getRider();
        rider.setReviewsSum(rider.getReviewsSum() + review);
        rider.setTotalNumReviews(rider.getTotalNumReviews() + 1);
        riderRepository.saveAndFlush(rider);

        log.info("PURCHASE SERVICE: Review for rider saved successfully");
        return purchase;
    }

    public Purchase updatePurchaseStatus(String token) throws InvalidLoginException, ResourceNotFoundException {
        String email = jwtUserDetailsService.getEmailFromToken(token);

        Rider rider = riderRepository.findByEmail(email).orElseThrow(() -> {
            log.error("PURCHASE SERVICE: Invalid rider token, when updating purchase status");
            return new InvalidLoginException("There is no Rider associated with this token");
        });

        Purchase unfinished = purchaseRepository.findTopByRiderAndStatusIsNot(rider, Status.DELIVERED).orElseThrow(() -> {
            log.error("PURCHASE SERVICE: This rider hasn't accepted an order yet, when updating purchase status");
            return new ResourceNotFoundException("This rider hasn't accepted an order yet");
        });

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

        log.info("PURCHASE SERVICE: Purchase status updated successfully");

        return unfinished;
    }

    public Purchase getNewPurchase(String token) throws InvalidLoginException, ForbiddenRequestException, ResourceNotFoundException {
        String email = jwtUserDetailsService.getEmailFromToken(token);
        Rider rider = riderRepository.findByEmail(email).orElseThrow(() -> {
            log.error("PURCHASE SERVICE: Invalid rider token, when getting rider's new order");
            return new InvalidLoginException("There is no Rider associated with this token");
        });

        // verify if Rider has any purchase to deliver
        if (purchaseRepository.findTopByRiderAndStatusIsNot(rider, Status.DELIVERED).isPresent()) {
            log.error("PURCHASE SERVICE: Rider still has an order to deliver, when getting rider's new order");
            throw new ForbiddenRequestException("This rider still has an order to deliver");
        }

        // get available order for rider
        Purchase purch = purchaseRepository.findTopByRiderIsNullOrderByDate().orElseThrow(() -> {
            log.error("PURCHASE SERVICE: No available orders, when getting rider's new order");
            return new ResourceNotFoundException("There are no more orders available");
        });

        // accept order
        purch.setRider(rider);
        purch.setStatus(Status.ACCEPTED);

        Store store = purch.getStore();

        StringBuilder url  = new StringBuilder().append(store.getStoreUrl())
                .append("setRider?serverOrderId=")
                .append( purch.getId());

        specificService.setRiderName(rider.getName(), url.toString());

        purchaseRepository.save(purch);

        log.info("PURCHASE SERVICE: Rider successfully accepted a new order to deliver");
        return purch;
    }

    public Purchase getCurrentPurchase(String token) throws InvalidLoginException, ResourceNotFoundException {
        String email = jwtUserDetailsService.getEmailFromToken(token);
        Rider rider = riderRepository.findByEmail(email).orElseThrow(() -> {
            log.error("PURCHASE SERVICE: Invalid rider token, when getting rider's current purchase");
            return new InvalidLoginException("There is no Rider associated with this token");
        });

        Purchase response = purchaseRepository.findTopByRiderAndStatusIsNot(rider, Status.DELIVERED).orElseThrow(() -> {
            log.error("PURCHASE SERVICE: Rider has no orders, when getting rider's current purchase");
            return new ResourceNotFoundException("This rider hasn't accepted an order yet");
        });

        log.info("PURCHASE SERVICE: Successfully retrieved rider's current order");
        return response;
    }

    public Map<String, Object> getLastOrderForRider(Integer pageNo, Integer pageSize, String riderToken) throws InvalidLoginException {
        String email = jwtUserDetailsService.getEmailFromToken(riderToken);

        Rider rider = riderRepository.findByEmail(email).orElseThrow(() -> {
            log.error("PURCHASE SERVICE: Invalid rider token, when getting last order for rider");
            return new InvalidLoginException("There is no Rider associated with this token");
        });

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

        log.info("PURCHASE SERVICE: Successfully retrieved rider's last order");
        return response;
    }


    public Purchase receiveNewOrder(String storeToken, Map<String, Object> data) throws InvalidValueException, InvalidLoginException {
        Store store = jwtUserDetailsService.getStoreFromToken(storeToken);
        if (store == null) {
            log.error("PURCHASE SERVICE: Invalid store token, when store tried to get new order");
            throw new InvalidLoginException("There is no Store associated with this token");
        }

        String error = "invalid data";

        Object personName = Optional.ofNullable(data.get("personName")) .orElseThrow(() -> {
            log.error("PURCHASE SERVICE: Invalid data, personName, when store tried to get new order");
            return new InvalidValueException(error);
        });

        if (!(personName instanceof String)) {
            log.error("PURCHASE SERVICE: Invalid data when store tried to get new order -> person name is not of type String");
            throw new InvalidValueException(error);
        }

        Object address = Optional.ofNullable(data.get("address")).orElseThrow(() -> {
            log.error("PURCHASE SERVICE: Invalid data, address, when store tried to get new order");
            return new InvalidValueException(error);
        });

        Address addr = new Address();
        try {
            addr.setAddress(((Map<String, String>) address).get("address"));
            addr.setCity(((Map<String, String>) address).get("city"));
            addr.setCountry(((Map<String, String>) address).get("country"));
            addr.setPostalCode(((Map<String, String>) address).get("postalCode"));
        } catch (Exception ex) {
            log.error("PURCHASE SERVICE: Invalid data, address, when store tried to get new order");
            throw new InvalidValueException(error);
        }

        addressRepository.save(addr);
        Purchase purchase = new Purchase(addr, store, (String) personName);
        purchaseRepository.save(purchase);

        log.info("PURCHASE SERVICE: Store successfully retrieved newest order");
        return purchase;
    }

    public Map<String, Object> getTop5Cities() {
        Map<String, Object> response = new HashMap<>();
        List<Object[]> repositoryResponse = purchaseRepository.getTopFiveCitiesOfPurchases();

        for(Object[] p : repositoryResponse) {
            if (p != null && response.size() < 5) {
                response.put((String) p[0], p[1]);
            }
        }

        log.info("PURCHASE SERVICE: Manager successfully retrieved top 5 cities where orders were made");
        return response ;
    }

    public Purchase getNewPurchaseLoc(String token, Double latitude, Double longitude) throws InvalidLoginException, ForbiddenRequestException, ResourceNotFoundException, InvalidValueException {
        String email = jwtUserDetailsService.getEmailFromToken(token);
        Rider rider = riderRepository.findByEmail(email).orElseThrow(() -> {
            log.error("PURCHASE SERVICE: Invalid rider token when trying to get new purchase location");
            return new InvalidLoginException("There is no Rider associated with this token");
        });

        if (latitude > 90 || latitude < -90 || longitude > 180 || longitude < -180) {
            log.error("PURCHASE SERVICE: Invalid values for coordinates when trying to get new purchase location");
            throw new InvalidValueException("Invalid values for coordinates");
        }

            // verify if Rider has any purchase to deliver
        if (purchaseRepository.findTopByRiderAndStatusIsNot(rider, Status.DELIVERED).isPresent()) {
            log.error("PURCHASE SERVICE: Rider still has an order to deliver, when trying to get new purchase location");
            throw new ForbiddenRequestException("This rider still has an order to deliver");
        }

        // get available order for rider
        Pageable paging = PageRequest.of(0, 15, Sort.by("date").ascending());
        Page<Purchase> possible = purchaseRepository.findAllByRiderIsNullOrderByDate(paging);
        Purchase purch = possible.stream().min(Comparator.comparingDouble(purchase ->
                distance(purchase.getStore(), latitude, longitude))).orElseThrow(() -> {
            log.error("PURCHASE SERVICE: No available orders, when trying to get new purchase location");
            return new ResourceNotFoundException("There are no more orders available");
        });

        // accept order
        purch.setRider(rider);
        purch.setStatus(Status.ACCEPTED);

        Store store = purch.getStore();


        StringBuilder url  = new StringBuilder().append(store.getStoreUrl())
                .append("setRider?serverOrderId=")
                .append( purch.getId());

        specificService.setRiderName(rider.getName(), url.toString());


        purchaseRepository.save(purch);

        log.info("PURCHASE SERVICE: Rider successfully retrieved new order location");
        return purch;
    }

    public static double distance(Store store, double x2, double y2) {
        return Math.sqrt((y2 - store.getLongitude()) * (y2 - store.getLongitude()) + (x2 - store.getLatitude()) * (x2 - store.getLatitude()));
    }

}

