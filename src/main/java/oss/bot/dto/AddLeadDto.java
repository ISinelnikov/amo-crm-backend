package oss.bot.dto;

import oss.backend.util.OSSStringUtils;
import oss.backend.util.MappingUtils;

import javax.annotation.Nullable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Objects.requireNonNull;

public record AddLeadDto(
        String token, @Nullable String clientName, @Nullable String clientPhone, @Nullable String comment
) {
    @JsonCreator
    public AddLeadDto(
            @JsonProperty("token") String token,
            @JsonProperty("clientName") @Nullable String clientName,
            @JsonProperty("clientPhone") @Nullable String clientPhone,
            @JsonProperty("comment") @Nullable String comment
    ) {
        this.token = requireNonNull(token, "token can't be null.");
        this.clientName = OSSStringUtils.valueToNull(clientName);
        this.clientPhone = requireNonNull(clientPhone, "clientPhone can't be null.");
        this.comment = OSSStringUtils.valueToNull(comment);
    }

    @Override
    public String toString() {
        return MappingUtils.convertObjectToJson(this);
    }
}
