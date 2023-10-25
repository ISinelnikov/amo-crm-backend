package oss.backend.domain.source;

import java.util.stream.Stream;

public enum MarkerSourceType {
    CONTAINS,
    MATCH;

    public static MarkerSourceType of(String value) {
        return Stream.of(values())
                .filter(markerSourceType -> markerSourceType.name().equals(value))
                .findFirst()
                .orElse(CONTAINS);
    }
}
