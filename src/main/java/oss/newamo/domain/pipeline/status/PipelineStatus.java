package oss.newamo.domain.pipeline.status;

import java.time.ZonedDateTime;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public record PipelineStatus(
        long id, String name, long sortId, @Nullable String color, ZonedDateTime creationDate
) {
    public PipelineStatus(long id, String name, long sortId, @Nullable String color, ZonedDateTime creationDate) {
        this.id = id;
        this.name = requireNonNull(name, "name can't be null.");
        this.sortId = sortId;
        this.color = color;
        this.creationDate = requireNonNull(creationDate, "creationDate can't be null.");
    }
}
