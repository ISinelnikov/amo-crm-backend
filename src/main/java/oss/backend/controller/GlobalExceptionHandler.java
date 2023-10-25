package oss.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import oss.backend.exception.BadRequestException;
import oss.backend.exception.InternalServerError;
import oss.backend.exception.NotFoundException;
import oss.backend.exception.UnauthorizedException;

@ControllerAdvice
class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final ResponseEntity<JsonResponse> INTERNAL_SERVER_ERROR = new ResponseEntity<>(
            new JsonResponse("Internal server error."), HttpStatus.INTERNAL_SERVER_ERROR
    );

    private static final ResponseEntity<JsonResponse> UNAUTHORIZED = new ResponseEntity<>(
            new JsonResponse("Unauthorized."), HttpStatus.UNAUTHORIZED
    );

    private static final ResponseEntity<JsonResponse> RESOURCE_NOT_FOUND = new ResponseEntity<>(
            new JsonResponse("Resource not found."), HttpStatus.NOT_FOUND
    );

    private static final ResponseEntity<JsonResponse> BAD_REQUEST = new ResponseEntity<>(
            new JsonResponse("Bad request."), HttpStatus.BAD_REQUEST
    );

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseBody
    public ResponseEntity<JsonResponse> handleUnauthorized() {
        return UNAUTHORIZED;
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseBody
    public ResponseEntity<JsonResponse> handleBadRequestException() {
        return BAD_REQUEST;
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseBody
    public ResponseEntity<JsonResponse> handleNotFoundException() {
        return RESOURCE_NOT_FOUND;
    }

    @ExceptionHandler(InternalServerError.class)
    @ResponseBody
    public ResponseEntity<JsonResponse> handleInternalServerError() {
        return INTERNAL_SERVER_ERROR;
    }

    public record JsonResponse(String error) {
    }
}
