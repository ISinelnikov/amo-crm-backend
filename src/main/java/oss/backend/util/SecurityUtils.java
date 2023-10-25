package oss.backend.util;

import oss.backend.exception.UnauthorizedException;
import oss.backend.security.User;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {
    }

    public static User getCurrentUser() {
        return getCurrentUser(SecurityContextHolder.getContext().getAuthentication());
    }

    public static User getCurrentUser(Authentication auth) {
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof User) {
                return (User) principal;
            }
        }
        throw new UnauthorizedException();
    }
}