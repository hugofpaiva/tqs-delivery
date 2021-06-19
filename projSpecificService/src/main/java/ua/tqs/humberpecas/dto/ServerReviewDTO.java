package ua.tqs.humberpecas.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;


@Data
public class ServerReviewDTO implements Serializable {

    @NotNull
    private String rider;

}
