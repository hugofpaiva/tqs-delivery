package ua.tqs.humberpecas.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotNull;


@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class AddressDTO {

    @JsonIgnore
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

    @JsonIgnore
    private long personID;
    

}
