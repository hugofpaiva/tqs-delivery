package ua.tqs.humberpecas.model;

import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;

public class JwtResponse implements Serializable {

    private static final long serialVersionUID = -8091879091924046844L;
    private final String jwttoken;
    private final GrantedAuthority type;
    private final String name;

    public JwtResponse(String jwttoken, GrantedAuthority type, String name) {
        this.jwttoken = jwttoken;
        this.type = type;
        this.name = name;
    }

    public String getToken() {
        return this.jwttoken;
    }

    public GrantedAuthority getType() {
        return type;
    }

    public String getName() {
        return name;
    }

}