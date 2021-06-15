package ua.tqs.humberpecas.dto;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotNull;


@Data
@RequiredArgsConstructor
public class AddressDTO {

    private Long addressId;

    @NonNull
    @NotNull(message = "Address is mandatory")
    private String address;

    @NonNull
    @NotNull(message = "Postal Code is mandatory")
    private String postalCode;

    @NonNull
    @NotNull(message = "City is mandatory")
    private String city;

    @NonNull
    @NotNull(message = "Country is mandatory")
    private String country;

    private long personID;
    

}
