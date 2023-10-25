package oss.backend.domain.dashboard;

import oss.backend.util.MappingUtils;

import java.time.ZonedDateTime;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public record LeadStatusEventInfo(
        long leadId, @Nullable String name,
        @Nullable String fromPipeline, @Nullable String toPipeline,
        @Nullable String fromStatus, @Nullable String toStatus,
        boolean deleted, ZonedDateTime dateCreate, ZonedDateTime dateUpdate
) {
    public LeadStatusEventInfo(long leadId, @Nullable String name, @Nullable String fromPipeline, @Nullable String toPipeline,
            @Nullable String fromStatus, @Nullable String toStatus, boolean deleted,
            ZonedDateTime dateCreate, ZonedDateTime dateUpdate) {
        this.leadId = leadId;
        this.name = name;
        this.fromPipeline = fromPipeline;
        this.toPipeline = toPipeline;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.deleted = deleted;
        this.dateCreate = requireNonNull(dateCreate, "dateCreate can't be null.");
        this.dateUpdate = requireNonNull(dateUpdate, "dateUpdate can't be null.");
    }

    @Override
    public String toString() {
        return MappingUtils.convertObjectToJson(this);
    }
}
