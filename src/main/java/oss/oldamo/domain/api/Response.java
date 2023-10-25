package oss.oldamo.domain.api;

import oss.oldamo.domain.api.common.Links;
import oss.backend.util.MappingUtils;

import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record Response<T>(@Nullable Links links, @Nullable Map<String, Collection<T>> embedded) {
    @JsonCreator
    public Response(
            @JsonProperty("links") @Nullable Links links,
            @JsonProperty("_embedded") @Nullable Map<String, Collection<T>> embedded
    ) {
        this.links = links;
        this.embedded = embedded;
    }

    @Override
    public String toString() {
        return MappingUtils.convertObjectToJson(this);
    }
}
