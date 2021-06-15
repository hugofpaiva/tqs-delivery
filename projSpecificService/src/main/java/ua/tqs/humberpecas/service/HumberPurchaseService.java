package ua.tqs.humberpecas.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.tqs.humberpecas.delivery.IDeliveryService;
import ua.tqs.humberpecas.dto.AddressDTO;
import ua.tqs.humberpecas.dto.PurchaseDTO;
import ua.tqs.humberpecas.dto.PurchaseDeliveryDTO;
import ua.tqs.humberpecas.exception.AccessNotAllowedException;
import ua.tqs.humberpecas.exception.InvalidLoginException;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.*;
import ua.tqs.humberpecas.repository.AddressRepository;
import ua.tqs.humberpecas.repository.PersonRepository;
import ua.tqs.humberpecas.repository.ProductRepository;
import ua.tqs.humberpecas.repository.PurchaseRepository;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
public class HumberPurchaseService {

    @Autowired
    private IDeliveryService deliveryService;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    public PurchaseStatus checkPurchaseStatus(long purchaseId) throws ResourceNotFoundException {

        // verificar se id é valido
        // se correto avnçar
        // se não lançar exeception

        // fazer um pedido a delivery service
        // verifcar responsta:
        // se devolver um valor retoronar
        // se  não lnçar um exeception

        return null;

    }

    public Purchase newPurchase(PurchaseDTO purchaseDTO, String userToken){

        var person = personRepository.findByEmail(jwtUserDetailsService.getEmailFromToken(userToken))
                .orElseThrow(()-> {
                    log.error("HumberPurchaseService: invalid user token" );
                    throw new InvalidLoginException("Invalid user token");
                });

        var address = addressRepository.findById(purchaseDTO.getAddressId())
                .orElseThrow(()-> {
                    log.error("HumberPurchaseService: invalid user addrees" );
                    throw new ResourceNotFoundException("Invalid Address");
                });

        if (!address.getPerson().getEmail().equals(person.getEmail())){

            log.error("HumberPurchaseService: Address don't belong to user " );
            throw new AccessNotAllowedException("Invalid Address");
        }

        List<Product> productList = productRepository.findAllById(purchaseDTO.getProductsId());

        if (productList.size() < purchaseDTO.getProductsId().size()){

            List<Long> differences = productList.stream().map(Product::getId).collect(Collectors.toList());
            purchaseDTO.getProductsId().forEach(differences::remove);

            log.error("HumberPurchaseService: Invalid Product Id " + differences);
            throw new ResourceNotFoundException("Invalid Product");

        }

        var purchaseDeliveryDTO = new PurchaseDeliveryDTO(
                person.getName(),
                purchaseDTO.getDate(),
                new AddressDTO(address.getAddress(), address.getPostalCode(), address.getCity(), address.getCountry())
        );


        var purchase = new Purchase(person, address, productList);
        purchase.setDate(purchaseDTO.getDate());

        purchase.setServiceOrderId(deliveryService.newOrder(purchaseDeliveryDTO));

        return purchaseRepository.save(purchase);
    }


    public List<Purchase> getUserPurchases(String userToken) throws ResourceNotFoundException{ return null; }

}
