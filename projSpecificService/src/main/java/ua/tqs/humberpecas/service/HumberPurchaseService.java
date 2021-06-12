package ua.tqs.humberpecas.services;

import org.springframework.stereotype.Service;
import ua.tqs.humberpecas.dto.PurchaseDTO;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.PurchaseStatus;
import ua.tqs.humberpecas.model.Purchase;

import java.util.List;

@Service
public class HumberPurchaseService {


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

    public void newPurchase(PurchaseDTO purchase){

        // validar dados
        // fazer mapeamentto

        // enviar os dados para o delivery service
        // receber o id de encomenda
        // guardar na bd
        //

    }

    public List<Purchase> getUserPurchases(String userToken) throws ResourceNotFoundException{ return null; }

}
