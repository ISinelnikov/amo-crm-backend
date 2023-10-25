package oss.backend.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import javax.annotation.Nullable;

public final class RepositoryUtils {
    private RepositoryUtils() {
    }

    @Nullable
    public static ZonedDateTime extractDateFromRS(ResultSet rs, String fieldName) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(fieldName);
        return timestamp == null ? null : ZonedDateTime.of(timestamp.toLocalDateTime(), DateUtils.DEFAULT_ZONE_ID);
    }

    @Nullable
    public static Boolean extractBooleanFromRS(ResultSet rs, String fieldName) throws SQLException {
        return getBoolean(rs, rs.getBoolean(fieldName));
    }

    @Nullable
    private static Boolean getBoolean(ResultSet rs, boolean value) throws SQLException {
        return rs.wasNull() ? null : value;
    }

    @Nullable
    public static Integer extractIntegerFromRS(ResultSet rs, String fieldName) throws SQLException {
        return getInteger(rs, rs.getInt(fieldName));
    }

    @Nullable
    private static Integer getInteger(ResultSet rs, int value) throws SQLException {
        return rs.wasNull() ? null : value;
    }

    @Nullable
    public static Long extractLongFromRS(ResultSet rs, String fieldName) throws SQLException {
        return getLong(rs, rs.getLong(fieldName));
    }

    @Nullable
    private static Long getLong(ResultSet rs, long value) throws SQLException {
        return rs.wasNull() ? null : value;
    }
}
