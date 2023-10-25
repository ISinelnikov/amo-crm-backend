package oss.backend.domain.roistat;

import oss.backend.util.OSSStringUtils;
import oss.backend.util.MappingUtils;
import oss.backend.util.NumberUtils;

import javax.annotation.Nullable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CallDto {
    @Nullable
    private final Long id;
    @Nullable
    private final String callee;
    @Nullable
    private final StaticSourceDto staticSource;

    @JsonCreator
    public CallDto(
            @JsonProperty("id") @Nullable String id,
            @JsonProperty("callee") @Nullable String callee,
            @JsonProperty("static_source") @Nullable StaticSourceDto staticSource
    ) {
        this.id = NumberUtils.longOrNull(id);
        this.callee = OSSStringUtils.valueToNull(callee);
        this.staticSource = staticSource;
    }

    @Nullable
    public Long getId() {
        return id;
    }

    @Nullable
    public String getCallee() {
        return callee;
    }

    @Nullable
    public StaticSourceDto getStaticSource() {
        return staticSource;
    }

    @Override
    public String toString() {
        return MappingUtils.convertObjectToJson(this);
    }
}
