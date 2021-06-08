package ua.tqs.humberpecas.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
public class PurchageDTO {

    @NotNull(message = "Client is mandatory")
    private long personId;

    @NotBlank(message = "Date is mandatory")
    private Date date;

    @NotNull(message = "Address is mandatory")
    private long addressId;

    @NotNull(message = "List of Products is mandatory")
    private List<Long> productsId;

    public PurchageDTO(Long personId, Date date, Long addressId, List<Long> productsId) {
        this.personId = personId;
        this.addressId = addressId;
        this.date = date;
        this.productsId = productsId;
    }

    public PurchageDTO(){ }
}
