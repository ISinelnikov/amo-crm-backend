package oss.backend.util;

import oss.oldamo.domain.api.common.CustomField;
import oss.oldamo.domain.api.common.CustomFieldValue;

import javax.annotation.Nullable;

public final class CustomFieldUtils {
    private CustomFieldUtils() {
    }

    @Nullable
    public static Long getFirstValueId(@Nullable CustomField field) {
        if (field == null) {
            return null;
        }
        return field.values()
                .stream()
                .findFirst()
                .map(CustomFieldValue::id)
                .orElse(null);
    }

    @Nullable
    public static String getFirstValue(@Nullable CustomField field) {
        if (field == null) {
            return null;
        }
        return field.values()
                .stream()
                .findFirst()
                .map(CustomFieldValue::value)
                .orElse(null);
    }

    @Nullable
    public static String getFirstValue(@Nullable oss.newamo.domain.CustomField field) {
        if (field == null) {
            return null;
        }
        return field.values()
                .stream()
                .findFirst()
                .map(oss.newamo.domain.CustomFieldValue::value)
                .orElse(null);
    }
}
