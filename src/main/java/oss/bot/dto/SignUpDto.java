package oss.bot.dto;

import oss.backend.util.MappingUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Objects.requireNonNull;

public record SignUpDto(String token, String referralCode, String name, String phone) {
    @JsonCreator
    public SignUpDto(
            @JsonProperty("token") String token,
            @JsonProperty("referralCode") String referralCode,
            @JsonProperty("name") String name,
            @JsonProperty("phone") String phone
    ) {
        this.token = requireNonNull(token, "Token can't be null.");
        this.referralCode = requireNonNull(referralCode, "Referral Token can't be null.");
        this.name = requireNonNull(name, "Name can't be null.");
        this.phone = requireNonNull(phone, "Phone can't be null.");
    }

    @Override
    public String toString() {
        return MappingUtils.convertObjectToJson(this);
    }
}
