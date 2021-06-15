package ua.tqs.humberpecas.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.Category;
import ua.tqs.humberpecas.model.Product;
import ua.tqs.humberpecas.repository.ProductRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HumberProductService {

    @Autowired
    private ProductRepository repository;

    public List<Product> getCatalog() {

        return repository.findAll();

    }

    public Product getProductById(long productId) throws ResourceNotFoundException {

        return repository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Invalid Product"));
    }


    public Map<String, Object> getProductsFiltered(int pageNo, int pageSize, String name, Double maxPrice, Double minPrice, String orderBy, Category category) {
        var sort = Sort.by("id").descending();

        if (orderBy != null && orderBy.equals("price")) {
            sort = Sort.by("price").ascending();
        }

        Pageable paging = PageRequest.of(pageNo, pageSize, sort);

        Page<Product> pagedResult;

        if (name != null && category != null) {
            pagedResult = repository.findAllByCategoryAndNameContainingAndPriceGreaterThanEqualAndPriceLessThanEqual(category, name, minPrice, maxPrice, paging);

        } else if (name != null) {
            pagedResult = repository.findAllByNameContainingIgnoreCaseAndPriceGreaterThanEqualAndPriceLessThanEqual(name, minPrice, maxPrice, paging);
        } else if (category != null){
            pagedResult = repository.findAllByCategoryAndPriceGreaterThanEqualAndPriceLessThanEqual(category, minPrice, maxPrice, paging);
        } else {
            pagedResult = repository.findAll(paging);
        }

        List<Product> responseList = new ArrayList<>();

        if (pagedResult.hasContent()) {
            responseList = pagedResult.getContent();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("products", responseList);
        response.put("currentPage", pagedResult.getNumber());
        response.put("totalItems", pagedResult.getTotalElements());
        response.put("totalPages", pagedResult.getTotalPages());

        return response;
    }


}
