package oss.bot.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Objects.requireNonNull;

public record SignInDto(String token, String accessToken) {
    @JsonCreator
    public SignInDto(
            @JsonProperty("token") String token,
            @JsonProperty("accessToken") String accessToken
    ) {
        this.token = requireNonNull(token, "Token can't be null.");
        this.accessToken = requireNonNull(accessToken, "Access Token can't be null.");
    }
}
