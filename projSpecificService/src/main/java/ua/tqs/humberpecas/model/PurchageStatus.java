package ua.tqs.humberpecas.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;


public enum PurchageStatus {

    PENDENT("pendent"),
    ACCEPTED("accepted"),
    PICKED_UP("picked_up"),
    DELIVERED("deliverd");

    private String status;

    private PurchageStatus(String status){
        this.status = status;
    }

    @JsonValue
    public String getStatus(){
        return status;
    }


}
