package oss.backend.domain.roistat;

import oss.backend.util.MappingUtils;

import javax.annotation.Nullable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import oss.backend.util.NumberUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static oss.backend.util.NumberUtils.longOrNull;

public class VisitDto {
    @Nullable
    private final Long id;
    @Nullable
    private final StaticSourceDto source;
    private final Set<Long> orderIds;

    @JsonCreator
    public VisitDto(
            @JsonProperty("id") @Nullable String id,
            @JsonProperty("source") @Nullable StaticSourceDto source,
            @JsonProperty("order_ids") @Nullable Collection<String> orderIds
    ) {
        this.id = longOrNull(id);
        this.source = source;
        this.orderIds = orderIds == null ? Collections.emptySet() : orderIds
                .stream()
                .map(NumberUtils::longOrNull)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Nullable
    public Long getId() {
        return id;
    }

    @Nullable
    public StaticSourceDto getSource() {
        return source;
    }

    public Set<Long> getOrderIds() {
        return orderIds;
    }

    @Override
    public String toString() {
        return MappingUtils.convertObjectToJson(this);
    }
}
