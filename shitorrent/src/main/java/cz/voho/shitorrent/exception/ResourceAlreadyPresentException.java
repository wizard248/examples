package cz.voho.shitorrent.exception;

/**
 * Created by vojta on 30/01/16.
 */
public class ResourceAlreadyPresentException extends Exception {
    public ResourceAlreadyPresentException(String key) {
        super(String.format("Resource %s already present.", key));
    }
}
