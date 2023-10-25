package oss.oldamo.domain.api.common;

import oss.backend.util.MappingUtils;

import javax.annotation.Nullable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Objects.requireNonNull;

public record CustomFieldValue(@Nullable Long id, @Nullable String value) {
    @JsonCreator
    public CustomFieldValue(@Nullable @JsonProperty("enum_id") Long id, @Nullable @JsonProperty("value") String value) {
        this.id = id;
        this.value = value;
    }

    @Override
    @Nullable
    @JsonProperty("enum_id")
    public Long id() {
        return id;
    }

    @Override
    public String toString() {
        return MappingUtils.convertObjectToJson(this);
    }

    public static CustomFieldValue of(long id) {
        return new CustomFieldValue(id, null);
    }

    public static CustomFieldValue of(String value) {
        return new CustomFieldValue(null, requireNonNull(value, "Value can't be null."));
    }

    public static CustomFieldValue of(long id, String value) {
        return new CustomFieldValue(id, requireNonNull(value, "Value can't be null."));
    }
}
