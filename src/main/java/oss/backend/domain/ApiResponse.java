package oss.backend.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse(@Nullable String message, @Nullable String details, @Nullable String reportUrl) {
    public static ApiResponse message(@Nullable String message) {
        return new ApiResponse(message, null, null);
    }

    public static ApiResponse details(@Nullable String details) {
        return new ApiResponse(null, details, null);
    }

    public static ApiResponse reportUrl(@Nullable String reportUrl) {
        return new ApiResponse(null, null, reportUrl);
    }

    public static ApiResponse empty() {
        return new ApiResponse(null, null, null);
    }
}
