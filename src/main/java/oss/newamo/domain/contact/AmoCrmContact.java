package oss.newamo.domain.contact;

import oss.backend.util.MappingUtils;
import oss.newamo.domain.CustomField;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Objects.requireNonNull;

public class AmoCrmContact {
    private final long id;
    private final String name;
    private final Collection<CustomField> customFields;

    @JsonCreator
    public AmoCrmContact(
            @JsonProperty("id") long id,
            @JsonProperty("name") String name,
            @JsonProperty("custom_fields_values") Collection<CustomField> customFields
    ) {
        this.id = id;
        this.name = requireNonNull(name, "name can't be null.");
        this.customFields = customFields;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Collection<CustomField> getCustomFields() {
        return customFields;
    }

    @Override
    public String toString() {
        return MappingUtils.convertObjectToJson(this);
    }
}
