package ua.tqs.humberpecas.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class ServerPurchaseDTO {

    @JsonProperty("orderId")
    private Long orderId;

    public ServerPurchaseDTO(long orderId){
        this.orderId = orderId;

    }
    public ServerPurchaseDTO(){ }

}
