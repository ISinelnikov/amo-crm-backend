package oss.oldamo.domain.api;

import oss.oldamo.domain.api.common.Links;
import oss.backend.util.MappingUtils;

import java.util.Collection;
import javax.annotation.Nullable;
import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateLeadResponse(@JsonProperty("id") long id, @JsonProperty("contact_id") @Nullable Long contactId,
                                 @JsonProperty("request_id") Collection<String> mergedRequestIds,
                                 @JsonProperty("_links") Links links) {
    @Override
    public String toString() {
        return MappingUtils.convertObjectToJson(this);
    }
}
