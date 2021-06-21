package ua.tqs.deliveryservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Store;
import ua.tqs.deliveryservice.repository.PurchaseRepository;
import ua.tqs.deliveryservice.repository.StoreRepository;

import java.util.*;
import java.util.concurrent.TimeUnit;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    public Map<String, Object> getStores(Integer pageNo, Integer pageSize) {
        Pageable paging = PageRequest.of(pageNo, pageSize);
        Page<Store> pagedResult = storeRepository.findAll(paging);

        List<Map<String, Object>> responseList = new ArrayList<>();
        if (pagedResult.hasContent()) {
            for (Store s : pagedResult.getContent()) {
                Map<String, Object> storeMap = s.getMap();
                storeMap.put("totalOrders", purchaseRepository.countPurchaseByStore(s));
                responseList.add(storeMap);
            }
        }
        Map<String, Object> response = new HashMap<>();
        response.put("stores", responseList);
        response.put("currentPage", pagedResult.getNumber());
        response.put("totalItems", pagedResult.getTotalElements());
        response.put("totalPages", pagedResult.getTotalPages());

        log.info("STORE SERVICE: Retrieved stores with success");
        return response;
    }

    public Map<String, Object> getStatistics() {
        long allPurchases = purchaseRepository.count();
        Purchase first = purchaseRepository.findTopByOrderByDate().orElse(null);
        Double avgPerWeek = null;
        if (first != null) {
            Date f = first.getDate();
            double weeksUntilNow = getNoWeeksUntilNow(f);
            avgPerWeek = allPurchases / weeksUntilNow;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("totalPurchases", allPurchases);
        response.put("avgPurchasesPerWeek", avgPerWeek);
        response.put("totalStores", storeRepository.count());

        log.info("STORE SERVICE: Retrieved statistics with success");
        return response;
    }

    /* --- helper --- */
    public double getNoWeeksUntilNow(Date from) {
        long diffInMillies = Math.abs(from.getTime() - new Date().getTime());
        return diffInMillies / (double) TimeUnit.DAYS.toMillis(1) / 7.0;
    }
}
