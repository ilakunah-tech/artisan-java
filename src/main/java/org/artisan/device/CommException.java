package org.artisan.device;

/**
 * Thrown when serial/COM port operations fail (open, read, write, or configuration).
 */
public class CommException extends RuntimeException {

    public CommException(String message) {
        super(message);
    }

    public CommException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommException(Throwable cause) {
        super(cause);
    }
}
