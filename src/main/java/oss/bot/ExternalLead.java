package oss.bot;

import oss.backend.util.DateUtils;

import javax.annotation.Nullable;
import java.time.ZonedDateTime;

import static oss.backend.util.DateUtils.REPORT_DATE_FORMATTER;

public record ExternalLead(
        long leadId, String clientName, String clientPhone, String name,
        @Nullable String manager, @Nullable Long statusId, @Nullable String status,
        @Nullable String rejectReason,
        ZonedDateTime lastUpdateDate, ZonedDateTime creationDate
) {
    public String formattedLastUpdateDate() {
        return DateUtils.formatDate(lastUpdateDate.toLocalDateTime(), REPORT_DATE_FORMATTER);
    }

    public String formattedCreationDate() {
        return DateUtils.formatDate(lastUpdateDate.toLocalDateTime(), REPORT_DATE_FORMATTER);
    }
}
