package oss.backend.util;

import javax.annotation.Nullable;
import org.springframework.util.StringUtils;

public final class NumberUtils {
    private NumberUtils() {
    }

    @Nullable
    public static Long longOrNull(@Nullable String value) {
        if (StringUtils.hasText(value)) {
            try {
                return Long.parseLong(value.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    @Nullable
    public static Double doubleOrZero(@Nullable String value) {
        if (StringUtils.hasText(value)) {
            try {
                return Double.parseDouble(value.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }
}
