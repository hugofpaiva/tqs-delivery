package ua.tqs.humberpecas.delivery;


import ua.tqs.humberpecas.dto.PurchaseDeliveryDTO;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.exception.UnreachableServiceException;
import ua.tqs.humberpecas.model.Category;
import ua.tqs.humberpecas.model.Review;


public interface IDeliveryService {

    Long newOrder(PurchaseDeliveryDTO purchase);
    Category checkOrderStatus(int orderId);
    void reviewRider(Review review) throws ResourceNotFoundException, UnreachableServiceException;


}
