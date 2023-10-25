package oss.newamo.domain.field;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AmoCrmFieldValue {
    private final long id;
    private final String value;

    @JsonCreator
    public AmoCrmFieldValue(
            @JsonProperty("id") long id,
            @JsonProperty("value") String value
    ) {
        this.id = id;
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public String getValue() {
        return value;
    }
}
