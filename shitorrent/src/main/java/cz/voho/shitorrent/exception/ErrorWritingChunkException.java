package cz.voho.shitorrent.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.nio.file.Path;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ErrorWritingChunkException extends IOException {
    public ErrorWritingChunkException(final Path path, final long offset, final int length, final Throwable cause) {
        super(String.format("Error writing to %s (bytes %d to %d).", path, offset, offset + length - 1), cause);
    }
}
