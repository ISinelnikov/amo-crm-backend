package oss.oldamo.domain.api.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import oss.backend.util.MappingUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

import static java.util.Objects.requireNonNull;

public record CustomField(long id, @Nullable String name, @Nullable String type, Collection<CustomFieldValue> values) {
    @JsonCreator
    public CustomField(
            @JsonProperty("field_id") long id,
            @Nullable @JsonProperty("field_name") String name,
            @Nullable @JsonProperty("field_type") String type,
            @JsonProperty("values") Collection<CustomFieldValue> values
    ) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.values = requireNonNull(values, "Values can't be null.");
    }

    @Override
    @JsonProperty("field_id")
    public long id() {
        return id;
    }

    @Override
    @JsonProperty("field_name")
    public String name() {
        return name;
    }

    @Override
    @JsonProperty("field_type")
    public String type() {
        return type;
    }

    @Override
    public String toString() {
        return MappingUtils.convertObjectToJson(this);
    }

    public static CustomField of(long id, CustomFieldValue value) {
        return new CustomField(id, null, null, List.of(requireNonNull(value, "Value can't be null.")));
    }
}