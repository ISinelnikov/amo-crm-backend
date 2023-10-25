package oss.newamo.domain.pipeline;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Objects.requireNonNull;

public class AmoCrmPipeline {
    private final long id;
    private final String name;
    private final long sortId;
    private final long ownerId;

    @JsonCreator
    public AmoCrmPipeline(
            @JsonProperty("id") long id,
            @JsonProperty("name") String name,
            @JsonProperty("sort") long sortId,
            @JsonProperty("account_id") long ownerId
    ) {
        this.id = id;
        this.name = requireNonNull(name, "name can't be null.");
        this.sortId = sortId;
        this.ownerId = ownerId;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getSortId() {
        return sortId;
    }

    public long getOwnerId() {
        return ownerId;
    }
}
