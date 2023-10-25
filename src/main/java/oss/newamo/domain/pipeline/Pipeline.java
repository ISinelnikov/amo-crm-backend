package oss.newamo.domain.pipeline;

import java.time.ZonedDateTime;

import static java.util.Objects.requireNonNull;

public record Pipeline(long id, String name, long sortId, long statusesCount, ZonedDateTime creationDate) {
    public Pipeline(long id, String name, long sortId, long statusesCount, ZonedDateTime creationDate) {
        this.id = id;
        this.name = requireNonNull(name, "name can't be null.");
        this.sortId = sortId;
        this.statusesCount = statusesCount;
        this.creationDate = requireNonNull(creationDate, "creationDate can't be null.");
    }
}
