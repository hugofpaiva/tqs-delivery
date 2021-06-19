package ua.tqs.humberpecas.service;


import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.tqs.humberpecas.exception.InvalidLoginException;
import ua.tqs.humberpecas.exception.InvalidOperationException;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.Purchase;
import ua.tqs.humberpecas.model.PurchaseStatus;
import ua.tqs.humberpecas.repository.GenericRepository;
import ua.tqs.humberpecas.repository.PurchaseRepository;


@Log4j2
@Service
public class HumberGenericServer {

    @Autowired
    private GenericRepository genericRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    public Purchase updateOrderStatus(Long serverOrderId, String genericToken, PurchaseStatus purchaseStatus){

        genericRepository.findByToken(genericToken)
                .orElseThrow(()-> {
                    log.error("HumberGenericServer: invalid token" );
                    throw new InvalidLoginException("Invalid token");
                });

        Purchase purchase = purchaseRepository.findByServiceOrderId(serverOrderId)
                .orElseThrow(()-> {
                    log.error("HumberGenericServer: invalid purchase server id" );
                    throw new ResourceNotFoundException("Invalid purchase");
                });

        purchase.setStatus(purchaseStatus);

        return purchaseRepository.saveAndFlush(purchase);


    }


    public Purchase setRider(Long serverOrderId, String genericToken, String riderName){

        genericRepository.findByToken(genericToken)
                .orElseThrow(()-> {
                    log.error("HumberGenericServer: invalid token" );
                    throw new InvalidLoginException("Invalid token");
                });

        Purchase purchase = purchaseRepository.findByServiceOrderId(serverOrderId)
                .orElseThrow(()-> {
                    log.error("HumberGenericServer: invalid purchase server id" );
                    throw new ResourceNotFoundException("Invalid purchase");
                });

        if (purchase.getStatus() != PurchaseStatus.PENDENT){
            log.error("HumberGenericServer: set Rider of order already accepted" );
            throw new InvalidOperationException("Order Already Assigned");
        }

        purchase.setRiderName(riderName);
        purchase.setStatus(PurchaseStatus.ACCEPTED);

       return purchaseRepository.saveAndFlush(purchase);

    }




}
