package ua.tqs.humberpecas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class InvalidLoginException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    public InvalidLoginException(String message){
        super(message);
    }
}
