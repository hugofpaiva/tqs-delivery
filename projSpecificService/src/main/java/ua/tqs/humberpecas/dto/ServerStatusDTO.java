package ua.tqs.humberpecas.dto;

import lombok.Data;
import ua.tqs.humberpecas.model.PurchaseStatus;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class ServerStatusDTO implements Serializable {

    @NotNull
    private PurchaseStatus orderStatus;

    public ServerStatusDTO(PurchaseStatus orderStatus){
        this.orderStatus = orderStatus;
    }

    public ServerStatusDTO(){ }

}
