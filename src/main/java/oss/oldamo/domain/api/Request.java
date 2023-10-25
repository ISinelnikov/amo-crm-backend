package oss.oldamo.domain.api;

import oss.oldamo.domain.api.common.CustomField;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class Request {
    @Nullable
    protected final Collection<CustomField> customFields;
    @Nullable
    protected final Map<EmbeddedType, Collection<Request>> embeddedValues;

    protected Request(@Nullable Collection<CustomField> customFields,
            @Nullable Map<EmbeddedType, Collection<Request>> embeddedValues) {
        this.customFields = customFields;
        this.embeddedValues = embeddedValues;
    }

    @Nullable
    @JsonProperty("custom_fields_values")
    public Collection<CustomField> getCustomFields() {
        return customFields;
    }

    @Nullable
    @JsonProperty("_embedded")
    public Map<String, Collection<Request>> getEmbeddedValues() {
        return embeddedValues == null ? null : embeddedValues.keySet()
                .stream()
                .collect(Collectors.toMap(type -> type.name().toLowerCase(), embeddedValues::get));
    }
}
