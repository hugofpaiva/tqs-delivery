package ua.tqs.humberpecas.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.util.stream.IntStream;

public enum PurchageStatus {

    PENDENT,
    ACCEPTED,
    PICKED_UP,
    DELIVERED;


}
