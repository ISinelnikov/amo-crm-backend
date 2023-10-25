package oss.backend.domain.roistat;

import oss.backend.util.MappingUtils;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import com.fasterxml.jackson.annotation.JsonProperty;

public record StaticSourceDto(
        @Nullable String systemName, @Nullable String displayName, List<String> displayNameByLevel
) {
    public StaticSourceDto(@JsonProperty("system_name") @Nullable String systemName,
            @JsonProperty("display_name") @Nullable String displayName,
            @JsonProperty("display_name_by_level") @Nullable List<String> displayNameByLevel) {
        this.systemName = systemName;
        this.displayName = displayName;
        this.displayNameByLevel = displayNameByLevel != null ? displayNameByLevel : Collections.emptyList();
    }

    @Override
    public String toString() {
        return MappingUtils.convertObjectToJson(this);
    }
}
