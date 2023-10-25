package oss.backend.exception;

import java.io.Serial;

public class BadRequestException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1;

    public BadRequestException() {
        this(null);
    }

    public BadRequestException(String message) {
        super(message);
    }
}
