package cz.voho.shitorrent.exception;

import java.nio.file.Path;

/**
 * Created by vojta on 18/01/16.
 */
public class CannotSeedException extends Exception {
    public CannotSeedException(Path path, Throwable cause) {
        super(String.format("Cannot seed path: %s", path), cause);
    }

    public CannotSeedException(Path path, String reason) {
        super(String.format("Cannot seed path %s: %s", path, reason));
    }

    public CannotSeedException(final Throwable cause) {
        super(cause);
    }
}
