package oss.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException e) throws IOException {
        processError(HttpStatus.UNAUTHORIZED, request, response, e);
    }

    private static void processError(HttpStatus httpStatus, HttpServletRequest request, HttpServletResponse response,
                                     RuntimeException e) throws IOException {
        response.setStatus(httpStatus.value());
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.getWriter().write("{}");
    }
}