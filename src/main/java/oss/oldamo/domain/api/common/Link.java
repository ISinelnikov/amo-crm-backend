package oss.oldamo.domain.api.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Objects.requireNonNull;

public record Link(String href) {
    @JsonCreator
    public Link(@JsonProperty("href") String href) {
        this.href = requireNonNull(href, "Href can't be null.");
    }
}
