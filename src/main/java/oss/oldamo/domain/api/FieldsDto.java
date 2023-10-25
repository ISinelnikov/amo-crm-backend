package oss.oldamo.domain.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;
import java.util.Collection;

import static java.util.Objects.requireNonNull;

public record FieldsDto(Embedded embedded) {
    @JsonCreator
    public FieldsDto(@JsonProperty("_embedded") Embedded embedded) {
        this.embedded = requireNonNull(embedded, "Embedded can't be null.");
    }

    public record Embedded(Collection<CustomFields> fields) {
        @JsonCreator
        public Embedded(@JsonProperty("custom_fields") Collection<CustomFields> fields) {
            this.fields = requireNonNull(fields, "Fields can't be null.");
        }
    }

    public record CustomFields(long id, String name, String type,
                               @Nullable Collection<CustomValues> values) {
        @JsonCreator
        public CustomFields(
                @JsonProperty("id") long id,
                @JsonProperty("name") String name,
                @JsonProperty("type") String type,
                @JsonProperty("enums") @Nullable Collection<CustomValues> values
        ) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.values = values;
        }
    }

    public record CustomValues(@JsonProperty("id") long id, @JsonProperty("value") String value) {
    }
}
