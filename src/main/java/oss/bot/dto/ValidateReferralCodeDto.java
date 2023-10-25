package oss.bot.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Objects.requireNonNull;

public record ValidateReferralCodeDto(String token, String referralCode) {
    @JsonCreator
    public ValidateReferralCodeDto(@JsonProperty("token") String token, @JsonProperty("referralCode") String referralCode) {
        this.token = requireNonNull(token, "token can't be null.");
        this.referralCode = requireNonNull(referralCode, "referralCode can't be null.");
    }
}
