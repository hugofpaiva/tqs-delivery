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



    // TODO: Assumir que o id é o mesmo (mantem-se) e apenas se alteram os dados
    @PutMapping("/updateAddress")
    public ResponseEntity<HttpStatus> updateUserAddress(@RequestParam long userId, @Valid @RequestBody Address address){

        return null;
    }

    @GetMapping("/address")
    public ResponseEntity<List<Address>> getUserAddress(@RequestParam long userId){

        return null;
    }

    @GetMapping("/addressDetails")
    public ResponseEntity<Address> getAddressDetails(@RequestParam long userId){

        return null;
    }

    @PostMapping("/addAddress")
    public ResponseEntity<HttpStatus> addNewAddress(@RequestParam long userId, @Valid @RequestBody Address address){

        return null;
    }


    // TODO: ver melhor opção ( ter uma especie de observer que o delivery notificaria caso a order mudasse de status
    // e a order passaria tambem a ter um status ou só fazer um pedido ao delivery service)

    @GetMapping("/order")
    public ResponseEntity<PurchageStatus> getOrderStatus(@RequestParam long orderId){

        return null;
    }

    // TODO: Ver qual a melhor opção (receber um objeto review ou receber os dados e criar no controller)s

    @PostMapping("/newReview")
    public ResponseEntity<HttpStatus> giveReview(@Valid @RequestBody Review Review){

        // enviar pra o service que vai verificar se o order id
        // se sim não acontece nada
        // se não o é lamcçada uma execption

        return null;
    }



}
