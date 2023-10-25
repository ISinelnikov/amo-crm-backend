package oss.backend.domain.roistat;

import java.util.List;

public record Filter(List<List<String>> filters) {
    public static Filter orderId(long orderId) {
        return new Filter(List.of(List.of("order_id", "=", String.valueOf(orderId))));
    }

    public static Filter id(long id) {
        return new Filter(List.of(List.of("id", "=", String.valueOf(id))));
    }
}
