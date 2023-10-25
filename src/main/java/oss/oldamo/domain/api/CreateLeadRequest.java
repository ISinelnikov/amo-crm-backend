package oss.oldamo.domain.api;

import oss.oldamo.domain.api.common.CustomField;
import oss.backend.util.MappingUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Objects.requireNonNull;

public class CreateLeadRequest extends Request {
    private final String name;
    @Nullable
    private final Long pipelineId;
    @Nullable
    private final Long pipelineStatusId;
    private final String requestId;

    public CreateLeadRequest(String name, @Nullable Long pipelineId, @Nullable Long pipelineStatusId,
            @Nullable Collection<CustomField> customFields, @Nullable Map<EmbeddedType, Collection<Request>> embeddedValues,
            String requestId) {
        super(customFields, embeddedValues);
        this.name = name;
        this.pipelineId = pipelineId;
        this.pipelineStatusId = pipelineStatusId;
        this.requestId = requireNonNull(requestId);
    }

    public String getName() {
        return name;
    }

    @Nullable
    @JsonProperty("pipeline_id")
    public Long getPipelineId() {
        return pipelineId;
    }

    @Nullable
    @JsonProperty("status_id")
    public Long getPipelineStatusId() {
        return pipelineStatusId;
    }

    @JsonProperty("request_id")
    public String getRequestId() {
        return requestId;
    }

    @Override
    public String toString() {
        return MappingUtils.convertObjectToJson(this);
    }

    public static class Builder {
        private String name;
        @Nullable
        private Long pipelineId;
        @Nullable
        private Long pipelineStatusId;
        @Nullable
        private Collection<CustomField> customFields;
        @Nullable
        private Map<EmbeddedType, Collection<Request>> embeddedValues;
        private String requestId;

        private Builder() {
        }

        public Builder setName(String name) {
            this.name = requireNonNull(name, "Name can't be null.");
            return this;
        }

        public Builder setPipelineId(@Nullable Long pipelineId) {
            this.pipelineId = pipelineId;
            return this;
        }

        public Builder setPipelineStatusId(@Nullable Long pipelineStatusId) {
            this.pipelineStatusId = pipelineStatusId;
            return this;
        }

        public Builder setCustomFields(@Nullable Collection<CustomField> customFields) {
            this.customFields = customFields;
            return this;
        }

        public Builder addEmbeddedValue(EmbeddedType type, Request value) {
            if (embeddedValues == null) {
                embeddedValues = new HashMap<>();
            }
            if (embeddedValues.containsKey(type)) {
                embeddedValues.get(type).add(value);
            } else {
                Collection<Request> values = new ArrayList<>();
                values.add(value);
                embeddedValues.put(type, values);
            }
            return this;
        }

        public Builder setRequestId(String requestId) {
            this.requestId = requireNonNull(requestId, "Request Id can't be null.");
            return this;
        }

        public CreateLeadRequest build() {
            return new CreateLeadRequest(name, pipelineId, pipelineStatusId, customFields, embeddedValues, requestId);
        }

        public static Builder getInstance() {
            return new Builder();
        }
    }
}
