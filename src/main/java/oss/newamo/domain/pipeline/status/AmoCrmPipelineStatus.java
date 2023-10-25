package oss.newamo.domain.pipeline.status;

import javax.annotation.Nullable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Objects.requireNonNull;

public class AmoCrmPipelineStatus {
    private final long id;
    private final long pipelineId;
    private final String name;
    private final long sortId;
    private final boolean editable;
    @Nullable
    private final String color;
    private final long ownerId;
    private final boolean unsorted;

    @JsonCreator
    public AmoCrmPipelineStatus(
            @JsonProperty("id") long id,
            @JsonProperty("pipeline_id") long pipelineId,
            @JsonProperty("name") String name,
            @JsonProperty("sort") long sortId,
            @JsonProperty("is_editable") boolean editable,
            @JsonProperty("color") @Nullable String color,
            @JsonProperty("account_id") long ownerId,
            @JsonProperty("type") long type
    ) {
        this.id = id;
        this.pipelineId = pipelineId;
        this.name = requireNonNull(name, "name can't be null.");
        this.sortId = sortId;
        this.editable = editable;
        this.color = color;
        this.ownerId = ownerId;
        this.unsorted = type == 1;
    }

    public long getId() {
        return id;
    }

    public long getPipelineId() {
        return pipelineId;
    }

    public String getName() {
        return name;
    }

    public long getSortId() {
        return sortId;
    }

    public boolean isEditable() {
        return editable;
    }

    @Nullable
    public String getColor() {
        return color;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public boolean isUnsorted() {
        return unsorted;
    }
}
