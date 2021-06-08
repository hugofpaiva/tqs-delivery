package ua.tqs.humberpecas.contoller;


import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.tqs.humberpecas.dto.AddressDTO;
import ua.tqs.humberpecas.dto.PersonDTO;
import ua.tqs.humberpecas.dto.PurchageDTO;
import ua.tqs.humberpecas.model.*;
import ua.tqs.humberpecas.service.HumberService;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import javax.validation.Valid;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("/shop") // TODO: Ver nome
public class HumberController {

    @Autowired
    private HumberService service;

    @GetMapping("/products/{prodId}")
    public ResponseEntity<Product> getProductById(@PathVariable long prodId) {
        try{

            var p = service.getProductById(prodId);

            return ResponseEntity.ok().body(p);

        }catch (ResourceNotFoundException e){

            log.error("Product with id" + prodId + " not found");

        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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
    public ResponseEntity<HttpStatus> register(@Valid @RequestBody PersonDTO person ){

        service.register(person);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/purchage")
    public ResponseEntity<HttpStatus> newOrder(@RequestBody PurchageDTO order){
        try{

            service.newPurchase(order);

            log.info("Order registed with success !");

            return new ResponseEntity<>(HttpStatus.OK);


        } catch (Exception e){

            log.error("Invalid Purchage");
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/purchageList")
    public ResponseEntity<List<Purchase>> getUserPurchages(@RequestParam long userId){

        return null;
    }



    // TODO: Assumir que o id é o mesmo (mantem-se) e apenas se alteram os dados
    @PutMapping("/updateAddress")
    public ResponseEntity<HttpStatus> updateUserAddress(@RequestParam long userId, @Valid @RequestBody AddressDTO address){

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
    public ResponseEntity<HttpStatus> addNewAddress(@RequestParam long userId, @Valid @RequestBody AddressDTO address){

        return null;
    }


    // TODO: ver melhor opção ( ter uma especie de observer que o delivery notificaria caso a order mudasse de status
    // e a order passaria tambem a ter um status ou só fazer um pedido ao delivery service)

    @GetMapping("/order")
    public ResponseEntity<String> getOrderStatus(@RequestParam long orderId){

        try{

            var status = service.checkPurchageStatus(orderId);
            return ResponseEntity.ok(status.getStatus());

        }catch (ResourceNotFoundException e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    // TODO: Ver qual a melhor opção (receber um objeto review ou receber os dados e criar no controller)s

    @PostMapping("/newReview")
    public ResponseEntity<HttpStatus> giveReview(@Valid @RequestBody Review review){

        try{
            service.addReview(review);

        }catch (Exception e){

            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }



}
