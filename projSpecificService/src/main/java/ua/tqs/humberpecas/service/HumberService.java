package ua.tqs.humberpecas.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.tqs.humberpecas.delivery.IDeliveryService;
import ua.tqs.humberpecas.dto.PersonDTO;
import ua.tqs.humberpecas.dto.PurchageDTO;
import ua.tqs.humberpecas.model.*;
import ua.tqs.humberpecas.repository.PersonRepository;
import ua.tqs.humberpecas.repository.ProductRepository;
import ua.tqs.humberpecas.repository.PurchaseRepository;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;

import java.util.List;

@Service
public class HumberService {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private IDeliveryService service;


    public void register(PersonDTO user){

        // validar os dados (verificar se email ja existe na bd)

        // se tudo bem guardar na bd

        // se não lançar excetion
    }


    public void newPurchase(PurchageDTO purchase){

        // validar dados
        // fazer mapeamentto

        // enviar os dados para o delivery service
        // receber o id de encomenda
        // guardar na bd
        //

    }

    public void addReview( Review review){

        // enviar review pra o delivery service
    }

    public PurchageStatus checkPurchageStatus(long purchase_id){

        // verificar se id é valido
            // se correto avnçar
            // se não lançar exeception

        // fazer um pedido a delivery service
        // verifcar responsta:
            // se devolver um valor retoronar
            // se  não lnçar um exeception

        return null;

    }

    public List<Product> getCatolog(){

        // fazer pedido a bd

        return null;

    }


    public Product getProductById(long productId) throws ResourceNotFoundException {
        return null;
    }


    public List<Product> getProductsByCategory(Category category){
        return null;
    }

}
