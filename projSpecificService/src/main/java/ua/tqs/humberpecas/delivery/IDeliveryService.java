package ua.tqs.humberpecas.delivery;


import ua.tqs.humberpecas.model.Category;
import ua.tqs.humberpecas.model.Purchase;
import ua.tqs.humberpecas.model.Review;


public interface IDeliveryService {

    void connectDeliveryService();
    void newOrder(Purchase purchase);
    Category checkOrderStatus(int order_id);
    void reviewRider(Review review);


}
