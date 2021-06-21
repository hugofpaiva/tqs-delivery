package ua.tqs.humberpecas.service;

import lombok.extern.log4j.Log4j2;
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
@Log4j2
public class HumberProductService {

    @Autowired
    private ProductRepository repository;

    public List<Product> getCatalog() {
        log.info("HUMBER PRODUCT SERVICE: Successfully retrieved catalog");
        return repository.findAll();
    }

    public Product getProductById(long productId) throws ResourceNotFoundException {
        Product response = repository.findById(productId).orElseThrow(() -> {
            log.error("HUMBER PRODUCT SERVICE: A product with that ID does not exist, when retrieving product by id");
            return new ResourceNotFoundException("Invalid Product");
        });

        log.info("HUMBER PRODUCT SERVICE: Successfully retrieved product by id");
        return response;
    }


    public Map<String, Object> getProductsFiltered(int pageNo, int pageSize, String name, Double maxPrice, Double minPrice, String orderBy, Category category) {
        var sort = Sort.by("id").descending();

        if (orderBy != null && orderBy.equals("price")) {
            sort = Sort.by("price").ascending();
        }

        Pageable paging = PageRequest.of(pageNo, pageSize, sort);

        Page<Product> pagedResult;

        if (name != null && category != null) {
            log.info("HUMBER PRODUCT SERVICE: Successfully retrieved filtered products by name and category ");
            pagedResult = repository.findAllByCategoryAndNameContainingIgnoreCaseAndPriceIsGreaterThanEqualAndPriceIsLessThanEqual(category, name, minPrice, maxPrice, paging);
        } else if (name != null) {
            log.info("HUMBER PRODUCT SERVICE: Successfully retrieved filtered products by name");
            pagedResult = repository.findAllByNameContainingIgnoreCaseAndPriceIsGreaterThanEqualAndPriceIsLessThanEqual(name, minPrice, maxPrice, paging);
        } else if (category != null){
            log.info("HUMBER PRODUCT SERVICE: Successfully retrieved filtered products by category");
            pagedResult = repository.findAllByCategoryAndPriceIsGreaterThanEqualAndPriceIsLessThanEqual(category, minPrice, maxPrice, paging);
        } else {
            log.info("HUMBER PRODUCT SERVICE: Successfully retrieved filtered products without filters");
            pagedResult = repository.findAllByPriceIsGreaterThanEqualAndPriceIsLessThanEqual(minPrice, maxPrice, paging);
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
