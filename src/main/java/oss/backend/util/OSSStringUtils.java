package oss.backend.util;

import javax.annotation.Nullable;
import org.springframework.util.StringUtils;

public final class OSSStringUtils {
    private OSSStringUtils() {
    }

    @Nullable
    public static String valueToNull(@Nullable String value) {
        if (StringUtils.hasText(value)) {
            return value.replaceAll("\\R", "").trim();
        }
        return null;
    }

    public static String valueToEmpty(@Nullable String value) {
        value = valueToNull(value);
        return value != null ? value : "";
    }
}
