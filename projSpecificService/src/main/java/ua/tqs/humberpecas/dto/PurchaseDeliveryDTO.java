package ua.tqs.humberpecas.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Data
public class PurchaseDeliveryDTO implements Serializable {

    @NotBlank(message = "personName is mandatory")
    private String personName;

    @NotBlank(message = "Date is mandatory")
    private Date date;

    @NotNull(message = "Address is mandatory")
    private AddressDTO address;


    public PurchaseDeliveryDTO(String personName, Date date, AddressDTO address) {
        this.personName = personName;
        this.date = date;
        this.address = address;
    }

    public PurchaseDeliveryDTO(){ }
}
