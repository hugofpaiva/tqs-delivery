package ua.tqs.humberpecas.contoller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.tqs.humberpecas.model.*;

import java.util.List;

@RestController
@RequestMapping("/shop") // TODO: Ver nome
public class HumberController {


    @GetMapping("/products/{prod_id}")
    public ResponseEntity<Product> getProductById(@PathVariable int prod_id){

        return null;
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts(@RequestParam(required = false) String category){

        return null;
    }

    @PostMapping("/register")
    public ResponseEntity<HttpStatus> register(@RequestBody Person person ){

        return null;
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
