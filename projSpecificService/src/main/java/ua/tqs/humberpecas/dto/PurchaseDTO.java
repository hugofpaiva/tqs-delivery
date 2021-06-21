package ua.tqs.humberpecas.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
public class PurchaseDTO {


    private long personId;

    private Date date;

    @NotNull(message = "Address is mandatory")
    private long addressId;

    @NotNull(message = "List of Products is mandatory")
    private List<Long> productsId;

    public PurchaseDTO(Long addressId, List<Long> productsId) {
        this.addressId = addressId;
        this.productsId = productsId;
    }

    public PurchaseDTO(){ }
}
