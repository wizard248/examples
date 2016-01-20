package cz.voho.shitorrent.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends Exception {
    public ResourceNotFoundException(final String keyNotFound) {
        super(String.format("Resource with key '%s' was not found.", keyNotFound));
    }
}
