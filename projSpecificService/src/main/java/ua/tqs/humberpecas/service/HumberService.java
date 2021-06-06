package ua.tqs.humberpecas.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.tqs.humberpecas.delivery.IDeliveryService;
import ua.tqs.humberpecas.model.*;
import ua.tqs.humberpecas.repository.PersonRepository;
import ua.tqs.humberpecas.repository.ProductRepository;
import ua.tqs.humberpecas.repository.PurchaseRepository;

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


    public void register(Person user){

        // validar os dados (verificar se email ja existe na bd)

        // se tudo bem guardar na bd

        // se não lançar excetion
    }


    public void newPurchase(Purchase purchase){

        // validar dados

        // enviar os dados para o delivery service
        // receber o id de encomenda
        // guardar na bd
        //

    }

    public void addReview( Review review){

        // enviar review pra o delivery service
    }

    public String checkPurchageStatus(int purchase_id){

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


    public Product findProductByName(String name){
        return null;
    }


    public List<Product> getProductsByCategory(Category category){
        return null;
    }

}
