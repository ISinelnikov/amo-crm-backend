package oss.newamo.domain.user;

import oss.backend.util.MappingUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Objects.requireNonNull;

public class AmoCrmUser {
    private final long id;
    private final String name;
    private final String email;

    @JsonCreator
    public AmoCrmUser(
            @JsonProperty("id") long id,
            @JsonProperty("name") String name,
            @JsonProperty("email") String email
    ) {
        this.id = id;
        this.name = requireNonNull(name, "name can't be null.");
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return MappingUtils.convertObjectToJson(this);
    }
}
