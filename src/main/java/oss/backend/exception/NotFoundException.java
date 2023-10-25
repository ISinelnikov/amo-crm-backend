package oss.backend.exception;

import java.io.Serial;

public class NotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1;

    public NotFoundException() {
        this(null);
    }

    public NotFoundException(String message) {
        super(message);
    }
}
