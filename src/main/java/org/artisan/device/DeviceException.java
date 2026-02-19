package org.artisan.device;

/**
 * Thrown when a device channel operation fails (open, read, etc.).
 */
public class DeviceException extends RuntimeException {

    public DeviceException(String message) {
        super(message);
    }

    public DeviceException(String message, Throwable cause) {
        super(message, cause);
    }
}
