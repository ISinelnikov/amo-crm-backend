package oss.backend.util;

import javax.annotation.Nullable;

public class WebhookUtils {
    public static String LEAD_PREFIX = "leads";
    public static String ADD_ACTION = "add";
    public static String UPDATE_ACTION = "update";
    public static String DELETE_ACTION = "delete";

    public static boolean isLeadId(String prefix, String action, String value) {
        if (value.startsWith(prefix)) {
            String[] split = prepareValue(value)
                    .replaceAll(" +", " ").split(" ");
            return split.length == 4 && split[1].equals(action) && split[3].equals("id");
        }
        return false;
    }

    @Nullable
    public static Long parseLeadId(String value) {
        return NumberUtils.longOrNull(prepareValue(value));
    }

    private static String prepareValue(String value) {
        return value.replace('[', ' ').replace(']', ' ').trim();
    }
}
