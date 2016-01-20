package cz.voho.shitorrent.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.nio.file.Path;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ErrorTruncatingFileException extends Exception {
    public ErrorTruncatingFileException(final Path path, final Throwable cause) {
        super(String.format("Error while truncating file %s.", path), cause);
    }
}
