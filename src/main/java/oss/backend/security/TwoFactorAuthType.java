package oss.backend.security;

import java.util.stream.Stream;

public enum TwoFactorAuthType {
    EMAIL, SMS, GA, NONE;

    public static TwoFactorAuthType of(String value) {
        return Stream.of(values())
                .filter(authType -> authType.name().equals(value))
                .findFirst()
                .orElse(NONE);
    }
}