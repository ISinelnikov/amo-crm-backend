package oss.bot.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ValidateSessionDto(String token) {
    @JsonCreator
    public ValidateSessionDto(@JsonProperty("token") String token) {
        this.token = Objects.requireNonNull(token, "token can't be null.");
    }
}
