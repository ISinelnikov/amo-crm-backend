package oss.oldamo.domain.api.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public record Links(Link self, @Nullable Link next) {
    @JsonCreator
    public Links(@JsonProperty("self") Link self, @JsonProperty("next") @Nullable Link next) {
        this.self = requireNonNull(self, "Self can't be null.");
        this.next = next;
    }
}
