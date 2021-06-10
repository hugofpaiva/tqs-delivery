package ua.tqs.deliveryservice.services;

import org.springframework.beans.factory.annotation.Autowired;
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
    JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    RiderRepository riderRepository;

    @Autowired
    PurchaseRepository purchaseRepository;

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
