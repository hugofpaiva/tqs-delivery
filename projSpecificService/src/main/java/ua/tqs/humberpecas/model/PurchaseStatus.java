package ua.tqs.humberpecas.model;

import com.fasterxml.jackson.annotation.JsonValue;


public enum PurchaseStatus {

    PENDENT("PENDENT"),
    ACCEPTED("ACCEPTED"),
    PICKED_UP("PICKED_UP"),
    DELIVERED("DELIVERED");

    private String status;

    private PurchaseStatus(String status){
        this.status = status;
    }

    @JsonValue
    public String getStatus(){
        return status;
    }


}
