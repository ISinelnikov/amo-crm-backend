package oss.backend.exception;

import java.io.Serial;

public class InternalServerError extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1;

    public InternalServerError(String message) {
        super(message);
    }
}
