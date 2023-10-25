package oss.backend.domain.source;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import oss.backend.util.MappingUtils;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public record VisitPatternItem(int orderId, Set<String> aliases, MarkerSourceType type) {
    public VisitPatternItem(int orderId, Set<String> aliases, MarkerSourceType type) {
        this.orderId = orderId;
        this.aliases = requireNonNull(aliases);
        this.type = requireNonNull(type);
    }

    public boolean matches(String value) {
        String lowerValue = value.toLowerCase();
        if (type == MarkerSourceType.MATCH) {
            return aliases.contains(lowerValue);
        }
        if (type == MarkerSourceType.CONTAINS) {
            return aliases.stream().anyMatch(lowerValue::contains);
        }
        return false;
    }

    @Override
    public String toString() {
        return MappingUtils.convertObjectToJson(this);
    }

    @JsonCreator
    public static VisitPatternItem of(
            @JsonProperty("orderId") int orderId,
            @JsonProperty("aliases") Collection<String> aliases,
            @JsonProperty("type") String type) {
        return new VisitPatternItem(orderId, collectionToLoweCase(aliases), MarkerSourceType.of(type));
    }

    public static VisitPatternItem of(int orderId, Collection<String> aliases, MarkerSourceType type) {
        return new VisitPatternItem(orderId, collectionToLoweCase(aliases), type);
    }

    private static Set<String> collectionToLoweCase(Collection<String> aliases) {
        return aliases.stream().map(String::toLowerCase).collect(Collectors.toSet());
    }
}
