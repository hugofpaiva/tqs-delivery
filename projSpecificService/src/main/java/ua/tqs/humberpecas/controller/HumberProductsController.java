package ua.tqs.humberpecas.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.Category;
import ua.tqs.humberpecas.model.Product;
import ua.tqs.humberpecas.service.HumberProductService;

import java.util.List;

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


    // TODO: se não houver produtos retornar uma lista vazia ou execption

    @GetMapping("/getAll")
    public ResponseEntity<List<Product>> getProducts(@RequestParam(required = false) Category category) throws ResourceNotFoundException {
        if (category != null){
            var p = service.getProductsByCategory(category);
            return ResponseEntity.ok().body(p);
        }

        return ResponseEntity.ok().body(service.getCatolog());
    }

}
