package ua.tqs.deliveryservice.specific;

import ua.tqs.deliveryservice.exception.InvalidValueException;
import ua.tqs.deliveryservice.exception.UnreachableServiceException;
import ua.tqs.deliveryservice.model.Status;

public interface ISpecificService {

    void updateOrderStatus(Status orderStatus, String storeUrl) throws UnreachableServiceException, InvalidValueException;
    void setRiderName(String rider, String storeUrl) throws UnreachableServiceException, InvalidValueException;

}
