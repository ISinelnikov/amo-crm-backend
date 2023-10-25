package oss.newamo.domain.field;

import java.util.Collection;
import javax.annotation.Nullable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AmoCrmField {
    private final long id;
    private final String name;
    private final String type;
    @Nullable
    private final Collection<AmoCrmFieldValue> values;

    @JsonCreator
    public AmoCrmField(
            @JsonProperty("id") long id,
            @JsonProperty("name") String name,
            @JsonProperty("type") String type,
            @JsonProperty("enums") @Nullable Collection<AmoCrmFieldValue> values
    ) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.values = values;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Nullable
    public Collection<AmoCrmFieldValue> getValues() {
        return values;
    }
}
