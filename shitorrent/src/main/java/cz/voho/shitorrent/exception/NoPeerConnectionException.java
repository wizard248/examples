package cz.voho.shitorrent.exception;

/**
 * Created by vojta on 21/01/16.
 */
public class NoPeerConnectionException extends Exception {
    public NoPeerConnectionException(String url, Throwable cause) {
        super(String.format("Could not connect to URL %s.", url), cause);
    }
}
