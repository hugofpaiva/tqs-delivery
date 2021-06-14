package ua.tqs.humberpecas.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.tqs.humberpecas.exception.InvalidParameterException;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.Category;
import ua.tqs.humberpecas.model.Product;
import ua.tqs.humberpecas.service.HumberProductService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product")
public class HumberProductsController {

    @Autowired
    private HumberProductService service;


    @GetMapping("/get/{prodId}")
    public ResponseEntity<Product> getProductById(@PathVariable long prodId) throws ResourceNotFoundException {

        var p = service.getProductById(prodId);

        return ResponseEntity.ok().body(p);
    }


    @GetMapping("/getAll")
    public ResponseEntity<Map<String, Object>> getProducts(@RequestParam(defaultValue = "0") int pageNo,
                                                           @RequestParam(defaultValue = "10") int pageSize,
                                                           @RequestParam(required = false) String name,
                                                           @RequestParam(defaultValue = "100000") Integer maxPrice,
                                                           @RequestParam(defaultValue = "0") Integer minPrice,
                                                           @RequestParam(required = false) String orderBy,
                                                           @RequestParam(required = false) Category category) throws InvalidParameterException {
        if (pageNo < 0 || pageSize <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (minPrice < 0 || maxPrice < 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok().body(service.getProductsFiltered(pageNo, pageSize, name, maxPrice.doubleValue(), minPrice.doubleValue(), orderBy, category));
    }

}
