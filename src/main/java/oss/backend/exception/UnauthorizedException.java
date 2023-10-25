package oss.backend.exception;

import java.io.Serial;

public class UnauthorizedException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1;

    public UnauthorizedException() {
        this(null);
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
