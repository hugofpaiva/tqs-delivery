package ua.tqs.humberpecas.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;


@Data
public class ServerReviewDTO {
    @NotNull
    private String rider;

}
