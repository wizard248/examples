package cz.voho.shitorrent.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ChunkNotFoundException extends Exception {
    public ChunkNotFoundException(final String keyNotFound, final int indexNotFound) {
        super(String.format("Chunk %d of resource with key '%s' was not found.", indexNotFound, keyNotFound));
    }
}
