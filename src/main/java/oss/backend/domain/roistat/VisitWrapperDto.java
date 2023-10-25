package oss.backend.domain.roistat;

import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nullable;
import com.fasterxml.jackson.annotation.JsonProperty;

public record VisitWrapperDto(Collection<VisitDto> data) {
    public VisitWrapperDto(@JsonProperty("data") @Nullable Collection<VisitDto> data) {
        this.data = data == null ? Collections.emptyList() : data;
    }
}
