package ua.tqs.humberpecas.dto;



import lombok.Data;
import ua.tqs.humberpecas.dto.AddressDTO;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;


@Data
public class PersonDTO {

    @NotNull(message = "Username is mandatory")
    private String name;

    @NotNull(message = "Password is mandatory")
    @Size(min=8)
    private String pwd;

    @NotNull(message = "Email is mandatory")
    @Email
    private String email;

    private List<AddressDTO> addresses;


    public PersonDTO(String name, String pwd, String email, List<AddressDTO> addresses) {
        this.name = name;
        this.pwd = pwd;
        this.email = email;
        this.addresses = addresses;
    }

    public PersonDTO(){ }
}
