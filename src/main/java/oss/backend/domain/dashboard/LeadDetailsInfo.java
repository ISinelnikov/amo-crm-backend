package oss.backend.domain.dashboard;

import oss.backend.util.MappingUtils;

import java.time.ZonedDateTime;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;
import static oss.backend.util.OSSStringUtils.valueToNull;

public record LeadDetailsInfo(
        long leadId, String name, String link,
        @Nullable String pipeline, @Nullable String status, @Nullable String source,
        boolean qualified, boolean closed, @Nullable String rejectReason, ZonedDateTime createdDate
) {
    public LeadDetailsInfo(long leadId, String name, String link, @Nullable String pipeline, @Nullable String status,
            @Nullable String source, boolean qualified, boolean closed, @Nullable String rejectReason, ZonedDateTime createdDate) {
        this.leadId = leadId;
        this.name = requireNonNull(valueToNull(name), "name can't be null.");
        this.link = requireNonNull(valueToNull(link), "link can't be null.");
        this.pipeline = pipeline;
        this.status = status;
        this.source = source;
        this.qualified = qualified;
        this.closed = closed;
        this.rejectReason = rejectReason;
        this.createdDate = createdDate;
    }

    public String getQualifiedTooltip() {
        return qualified ? "Квалификация проставлена" : "Квалификация не проставлена";
    }

    public String getClosedTooltip() {
        return closed ? "Сделка закрыта" : "Сделка открыта";
    }

    @Override
    public String toString() {
        return MappingUtils.convertObjectToJson(this);
    }
}
