package ua.tqs.humberpecas.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import ua.tqs.humberpecas.delivery.IDeliveryService;
import ua.tqs.humberpecas.dto.AddressDTO;
import ua.tqs.humberpecas.dto.PurchaseDTO;
import ua.tqs.humberpecas.exception.InvalidLoginException;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.Person;
import ua.tqs.humberpecas.model.Purchase;
import ua.tqs.humberpecas.repository.PersonRepository;
import ua.tqs.humberpecas.dto.PurchaseDeliveryDTO;
import ua.tqs.humberpecas.exception.AccessNotAllowedException;
import ua.tqs.humberpecas.model.*;
import ua.tqs.humberpecas.repository.AddressRepository;
import ua.tqs.humberpecas.repository.ProductRepository;
import ua.tqs.humberpecas.repository.PurchaseRepository;

import java.util.*;

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

    public Purchase newPurchase(PurchaseDTO purchaseDTO, String userToken) {

        var person = personRepository.findByEmail(jwtUserDetailsService.getEmailFromToken(userToken))
                .orElseThrow(() -> {
                    log.error("HUMBER PURCHASE SERVICE: invalid user token");
                    throw new InvalidLoginException("Invalid user token");
                });

        var address = addressRepository.findById(purchaseDTO.getAddressId())
                .orElseThrow(() -> {
                    log.error("HUMBER PURCHASE SERVICE: invalid user address");
                    throw new ResourceNotFoundException("Invalid Address");
                });

        if (!address.getPerson().getEmail().equals(person.getEmail())) {

            log.error("HUMBER PURCHASE SERVICE: Address don't belong to user ");
            throw new AccessNotAllowedException("Invalid Address");
        }

        List<Long> productsIds = purchaseDTO.getProductsId();

        List<Product> productList = new ArrayList<>();
        for (Long productId : productsIds) {
            Product productToAdd = productRepository.findById(productId).orElseThrow(() -> {
                log.error("HUMBER PURCHASE SERVICE: Invalid Product Id found:" + productId);
                throw new ResourceNotFoundException("Invalid Product");
            });
            productList.add(productToAdd);
        }

        var purchaseDeliveryDTO = new PurchaseDeliveryDTO(
                person.getName(),
                purchaseDTO.getDate(),
                new AddressDTO(address.getAddress(), address.getPostalCode(), address.getCity(), address.getCountry())
        );


        var purchase = new Purchase(person, address, productList);

        purchase.setServiceOrderId(deliveryService.newOrder(purchaseDeliveryDTO));

        log.info("HUMBER PURCHASE SERVICE: Successfully saved new purchase");
        return purchaseRepository.save(purchase);
    }


    public Map<String, Object> getUserPurchases(Integer pageNo, Integer pageSize, String userToken) throws InvalidLoginException {
        String email = jwtUserDetailsService.getEmailFromToken(userToken);

        Person person = personRepository.findByEmail(email).orElseThrow(() -> {
            log.error("HUMBER PURCHASE SERVICE: Invalid person token");
            return new InvalidLoginException("There is no Person associated with this token");
        });

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by("date").descending());

        Page<Purchase> pagedResult = purchaseRepository.findAllByPerson(person, paging);

        List<Purchase> responseList = new ArrayList<>();

        if (pagedResult.hasContent()) {
            responseList = pagedResult.getContent();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("orders", responseList);
        response.put("currentPage", pagedResult.getNumber());
        response.put("totalItems", pagedResult.getTotalElements());
        response.put("totalPages", pagedResult.getTotalPages());
        response.put("reviewsGiven", purchaseRepository.countPurchaseByPersonAndRiderReviewNotNull(person));

        log.info("HUMBER PURCHASE SERVICE: Successfully retrieved user purchases");
        return response;
    }
}
