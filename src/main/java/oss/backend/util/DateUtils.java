package oss.backend.util;

import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DateUtils {
    private static final String BASE_DATE_PATTERN = "yyyy-MM-dd";
    private static final String REPORT_DATE_PATTERN = "dd.MM.yyyy HH:mm:ss";

    public static final DateTimeFormatter BASE_DATE_FORMATTER = DateTimeFormatter.ofPattern(BASE_DATE_PATTERN);
    public static final DateTimeFormatter REPORT_DATE_FORMATTER = DateTimeFormatter.ofPattern(REPORT_DATE_PATTERN);

    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Europe/Moscow");

    private DateUtils() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(DEFAULT_ZONE_ID);
    }

    public static ZonedDateTime ofUnixTimestamp(long value) {
        return Instant.ofEpochSecond(value).atZone(DEFAULT_ZONE_ID);
    }

    @Nullable
    public static LocalDateTime parseFromDate(@Nullable String date, DateTimeFormatter formatter) {
        LocalDateTime fromDate = parseDate(date, formatter);
        return fromDate == null ? null : fromDate.toLocalDate().atTime(LocalTime.MIN);
    }

    @Nullable
    public static LocalDateTime parseToDate(@Nullable String date, DateTimeFormatter formatter) {
        LocalDateTime toDate = parseDate(date, formatter);
        return toDate == null ? null : toDate.toLocalDate().atTime(LocalTime.MAX);
    }

    @Nullable
    public static LocalDateTime parseDate(@Nullable String date, DateTimeFormatter formatter) {
        if (StringUtils.hasText(date)) {
            try {
                return LocalDate.parse(date, formatter).atStartOfDay();
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    public static String formatDate(LocalDateTime date, DateTimeFormatter formatter) {
        return date.format(formatter);
    }
}
