package ua.tqs.deliveryservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.model.Manager;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.repository.ManagerRepository;
import ua.tqs.deliveryservice.repository.RiderRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ManagerService {
    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private RiderRepository riderRepository;

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    // int countPurchaseByStatus(Status s);
    // double getAverageReview();

    public Map<String, Object> getRidersInformation(Integer pageNo, Integer pageSize, String managerToken)
        throws InvalidLoginException {

        String email = jwtUserDetailsService.getEmailFromToken(managerToken);
        Manager manager = managerRepository.findByEmail(email).orElseThrow(() -> new InvalidLoginException("No manager with such token was found."));

        Pageable paging = PageRequest.of(pageNo, pageSize);
        Page<Rider> result = riderRepository.findAll(paging);
        List<Map<String, Object>> responseList = new ArrayList<>();

        if (result.hasContent()) {
            for (Rider r : result.getContent()){
                Map<String, Object> temp = new HashMap<>();
                temp.put("name", r.getName());

                List<Purchase> list = r.getPurchases();
                temp.put("numberOrders", list.isEmpty() ? 0 : list.size());

                long sum = r.getReviewsSum();
                long total = r.getTotalNumReviews();
                temp.put("average", total == 0 ? 0.0 : sum * 1.0 /total);

                responseList.add(temp);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("riders", responseList);
        response.put("currentPage", result.getNumber());
        response.put("totalItems", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());

        return response;
    }


}
