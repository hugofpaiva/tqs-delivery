package ua.tqs.humberpecas.model;

import com.fasterxml.jackson.annotation.JsonFormat;


@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum PurchageStatus {

    PENDENT(0),
    ACCEPTED(1),
    PICKED_UP(2),
    DELIVERED(3);

    private int status;
    private PurchageStatus(int status){
        this.status = status;
    }


    public int getStatus(){
        return this.status;
    }

    public void setStatus(int status){
        this.status = status;
    }
}
