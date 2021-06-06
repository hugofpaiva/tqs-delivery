package ua.tqs.humberpecas.contoller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.tqs.humberpecas.model.*;
import ua.tqs.humberpecas.service.HumberService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/shop") // TODO: Ver nome
public class HumberController {

    @Autowired
    private HumberService service;

    @GetMapping("/products/{prod_id}")
    public ResponseEntity<Product> getProductById(@PathVariable int prod_id){

        return null;
    }


    // TODO: se não houver produtos retornar uma lista vazia ou execption

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts(@RequestParam(required = false) Category category){
        if (category != null){

            return ResponseEntity.ok().body(service.getProductsByCategory(category));
        }
        return ResponseEntity.ok().body(service.getCatolog());
    }

    @PostMapping("/register")
    public ResponseEntity<HttpStatus> register(@Valid @RequestBody Person person ){

        service.register(person);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/newOrder")
    public ResponseEntity<HttpStatus> newOrder(@RequestBody Purchase order){

        return null;
    }

    @GetMapping("/purchages")
    public ResponseEntity<List<Purchase>> getUserPurchages(@RequestParam String user){

        return null;
    }


    // TODO: ver melhor opção ( ter uma especie de observer que o delivery notificaria caso a order mudasse de status
    // e a order passaria tambem a ter um status ou só fazer um pedido ao delivery service)

    @GetMapping("/order")
    public ResponseEntity<PurchageStatus> getOrderStatus(@RequestParam long orderId){


        return null;
    }

    // TODO: Ver qual a melhor opção (receber um objeto review ou receber os dados e criar no controller)s

    @PostMapping("/review")
    public ResponseEntity<HttpStatus> giveReview(@RequestBody Review Review){

        return null;
    }



}
