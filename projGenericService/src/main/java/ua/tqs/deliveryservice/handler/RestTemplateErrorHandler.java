package ua.tqs.deliveryservice.handler;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import ua.tqs.deliveryservice.exception.InvalidValueException;
import ua.tqs.deliveryservice.exception.ResourceNotFoundException;
import ua.tqs.deliveryservice.exception.UnreachableServiceException;

import java.io.IOException;

import static org.springframework.http.HttpStatus.Series.CLIENT_ERROR;
import static org.springframework.http.HttpStatus.Series.SERVER_ERROR;


@Log4j2
@Component
public class RestTemplateErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse httpResponse)
            throws IOException {

        return (
                httpResponse.getStatusCode().series() == CLIENT_ERROR
                        || httpResponse.getStatusCode().series() == SERVER_ERROR);
    }

    @Override
    public void handleError(ClientHttpResponse httpResponse) throws IOException {

        if (httpResponse.getStatusCode()
                .series() == SERVER_ERROR) {
            log.error("RestTemplateErrorHandler: Unreachable Service");
            throw new UnreachableServiceException("Internal error");
            // handle SERVER_ERROR
        } else if (httpResponse.getStatusCode()
                .series() == CLIENT_ERROR) {

            log.error("RestTemplateErrorHandler: Invalid Operation");
            throw new InvalidValueException("Invalid Operation");

        }
    }
}
