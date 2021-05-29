package ua.tqs.humberpecas.contoller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.tqs.humberpecas.model.Product;

import java.util.List;

@RestController
@RequestMapping("/shop") // TODO: Ver nome
public class HumberController {

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts(){

        return null;
    }

    @GetMapping("/products/{prod_id}")
    public ResponseEntity<Product> getProductById(@PathVariable int prod_id){

        return null;
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProductsByCategory(@RequestParam String category){

        return null;
    }

    @PostMapping("/register")
    public ResponseEntity<HttpStatus> register(@RequestBody )

}
