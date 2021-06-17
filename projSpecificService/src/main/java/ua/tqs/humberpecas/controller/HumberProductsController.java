package ua.tqs.humberpecas.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.tqs.humberpecas.model.Category;
import ua.tqs.humberpecas.service.HumberProductService;

import java.util.Map;

@RestController
@RequestMapping("/product")
public class HumberProductsController {

    @Autowired
    private HumberProductService service;


    @GetMapping("/getAll")
    public ResponseEntity<Map<String, Object>> getProducts(@RequestParam(defaultValue = "0") int pageNo,
                                                           @RequestParam(defaultValue = "9") int pageSize,
                                                           @RequestParam(required = false) String name,
                                                           @RequestParam(defaultValue = "100000") Integer maxPrice,
                                                           @RequestParam(defaultValue = "0") Integer minPrice,
                                                           @RequestParam(required = false) String orderBy,
                                                           @RequestParam(required = false) Category category) {
        if (pageNo < 0 || pageSize <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (minPrice < 0 || maxPrice < 0 || minPrice > maxPrice) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok().body(service.getProductsFiltered(pageNo, pageSize, name, maxPrice.doubleValue(), minPrice.doubleValue(), orderBy, category));
    }
}
