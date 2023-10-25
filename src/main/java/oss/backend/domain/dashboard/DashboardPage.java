package oss.backend.domain.dashboard;

import java.util.Collection;
import java.util.Collections;

public record DashboardPage<T>(int page, int pageSize, int totalCount, Collection<T> items) {
    public static <T> DashboardPage<T> empty() {
        return new DashboardPage<>(0, 0, 0, Collections.emptyList());
    }

    public static <T> DashboardPage<T> of(int page, int pageSize, int totalCount, Collection<T> items) {
        return new DashboardPage<>(page, pageSize, totalCount, items);
    }
}
