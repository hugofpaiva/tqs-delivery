package ua.tqs.deliveryservice.model;

import lombok.Data;
import javax.persistence.Entity;

@Data
@Entity
public class Manager extends Person {

    public Manager(String name, String pwd, String email) {
        super(name, pwd, email);
    }

    public Manager() {}
}
