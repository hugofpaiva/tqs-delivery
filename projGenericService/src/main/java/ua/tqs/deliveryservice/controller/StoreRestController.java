package ua.tqs.deliveryservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.tqs.deliveryservice.model.Address;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Store;
import ua.tqs.deliveryservice.repository.AddressRepository;
import ua.tqs.deliveryservice.repository.PurchaseRepository;
import ua.tqs.deliveryservice.repository.StoreRepository;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/store")
public class StoreRestController {

    @Autowired
    private AddressRepository addressRep;

    @Autowired
    private PurchaseRepository purchaseRep;

    @Autowired
    private StoreRepository storeRep;

    @PostMapping("/purchase")
    public ResponseEntity<Object> receivePurchase(@RequestBody Map<String, Map<String, Object>> data) {
        // System.out.println("!! " + data + " !!");

        /*
         * { "purchase" : {
         *          "date" : idkkk,
         *          "address" : {...}
         *          "idStore" : 3, <- este se calhar dps vai-se buscar à sessao
         *          "clientName" : João
         *      }
         * }
         */

        Map<String, Object> purchase = data.getOrDefault("purchase", null);
        if (purchase == null) new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        Date date = null;
        Address address = null;
        Store store = null;
        String clientName = null;

        try {
            date = (Date) purchase.getOrDefault("date", null);

            Object addressMap = purchase.getOrDefault("address", null);
            objectToAddress(addressMap, address); // <- TODO

            Long idStore = (Long) purchase.getOrDefault("idStore", null); // for now
            store = storeRep.findById(idStore).get();

            clientName = (String) purchase.getOrDefault("clientName", null);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (date == null || address == null || store == null || clientName == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        addressRep.save(address);
        Purchase pur = new Purchase(address, store, clientName);
        purchaseRep.save(pur);

        return null; // TODO: change
    }

    /* -- helper -- */
    private void objectToAddress(Object object, Address address) {
        ; // TODO
    }
}
