package ua.tqs.humberpecas.model;

import com.fasterxml.jackson.annotation.JsonValue;


public enum PurchaseStatus {

    PENDENT("pendent"),
    ACCEPTED("accepted"),
    PICKED_UP("picked_up"),
    DELIVERED("deliverd");

    private String status;

    private PurchaseStatus(String status){
        this.status = status;
    }

    @JsonValue
    public String getStatus(){
        return status;
    }


}
