package ua.tqs.humberpecas.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ua.tqs.humberpecas.dto.PurchaseDTO;
import ua.tqs.humberpecas.exception.InvalidLoginException;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.Person;
import ua.tqs.humberpecas.model.Purchase;
import ua.tqs.humberpecas.repository.PersonRepository;
import ua.tqs.humberpecas.repository.PurchaseRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HumberPurchaseService {

    @Autowired
    JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    PersonRepository personRepository;

    @Autowired
    PurchaseRepository purchaseRepository;

    public void newPurchase(PurchaseDTO purchase){

        // validar dados
        // fazer mapeamentto

        // enviar os dados para o delivery service
        // receber o id de encomenda
        // guardar na bd
        //

    }

    public Map<String, Object> getUserPurchases(Integer pageNo, Integer pageSize, String userToken) throws InvalidLoginException {
        String email = jwtUserDetailsService.getEmailFromToken(userToken);

        Person person = personRepository.findByEmail(email).orElseThrow(() -> new InvalidLoginException("There is no Person associated with this token"));

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

        return response;
    }
}
