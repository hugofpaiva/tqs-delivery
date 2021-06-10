package ua.tqs.deliveryservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ua.tqs.deliveryservice.exception.ForbiddenRequestException;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.exception.ResourceNotFoundException;
import ua.tqs.deliveryservice.model.Person;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.model.Status;
import ua.tqs.deliveryservice.repository.PersonRepository;
import ua.tqs.deliveryservice.repository.PurchaseRepository;
import ua.tqs.deliveryservice.repository.RiderRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.repository.PurchaseRepository;
import ua.tqs.deliveryservice.repository.RiderRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PurchaseService {

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private RiderRepository riderRepository;


    public Purchase updatePurchaseStatus(String token) throws InvalidLoginException, ResourceNotFoundException {
        String email = jwtUserDetailsService.getEmailFromToken(token);
        Rider rider = riderRepository.findByEmail(email).orElseThrow(() -> new InvalidLoginException("There is no Rider associated with this token"));
        Purchase unfinished = purchaseRepository.findTopByRiderAndStatusIsNot(rider, Status.DELIVERED).orElseThrow( ()-> new ResourceNotFoundException("This rider hasn't accepted an order yet"));

        unfinished.setStatus(Status.getNext(unfinished.getStatus()));
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
        Purchase unfinished = purchaseRepository.findTopByRiderAndStatusIsNot(rider, Status.DELIVERED).orElseThrow( ()-> new ResourceNotFoundException("This rider hasn't accepted an order yet"));
        return unfinished;
    }

    public Map<String, Object> getLastOrderForRider(Integer pageNo, Integer pageSize, String riderToken) throws InvalidLoginException {
        String email = jwtUserDetailsService.getEmailFromToken(riderToken);

        Rider rider = riderRepository.findByEmail(email).orElseThrow(() -> new InvalidLoginException("There is no Rider associated with this token"));

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by("date").descending());

        Page<Purchase> pagedResult = purchaseRepository.findAllByRider(rider, paging);

        List<Purchase> responseList = new ArrayList<>();

        if(pagedResult.hasContent()) {
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
