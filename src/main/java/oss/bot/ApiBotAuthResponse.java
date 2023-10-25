package oss.bot;

import javax.annotation.Nullable;

public record ApiBotAuthResponse(ApiBotAuthStatus status, @Nullable Object body) {
    public static ApiBotAuthResponse authorized(@Nullable Object body) {
        return new ApiBotAuthResponse(ApiBotAuthStatus.AUTHORIZED, body);
    }

    public static ApiBotAuthResponse unauthorized(ApiBothAuthMessage body) {
        return new ApiBotAuthResponse(ApiBotAuthStatus.UNAUTHORIZED, body);
    }

    public static ApiBotAuthResponse internalError(ApiBothAuthMessage body) {
        return new ApiBotAuthResponse(ApiBotAuthStatus.INTERNAL_ERROR, body);
    }

    public static ApiBotAuthResponse incorrectReferrerCode(ApiBothAuthMessage body) {
        return new ApiBotAuthResponse(ApiBotAuthStatus.INCORRECT_REFERRER_CODE, body);
    }

    public static ApiBotAuthResponse correctReferrerCode(ApiBothAuthMessage body) {
        return new ApiBotAuthResponse(ApiBotAuthStatus.CORRECT_REFERRER_CODE, body);
    }

    public static ApiBotAuthResponse leadCreated() {
        return new ApiBotAuthResponse(ApiBotAuthStatus.LEAD_CREATED, null);
    }

    public static ApiBotAuthResponse userAlreadyExist() {
        return new ApiBotAuthResponse(ApiBotAuthStatus.INTERNAL_ERROR, null);
    }

    public enum ApiBotAuthStatus {
        AUTHORIZED,
        UNAUTHORIZED,

        LEAD_CREATED,

        INCORRECT_REFERRER_CODE,
        CORRECT_REFERRER_CODE,

        INTERNAL_ERROR,
    }

    public record ApiBothAuthMessage(String message) {
        public static ApiBothAuthMessage of(String message) {
            return new ApiBothAuthMessage(message);
        }
    }
}
