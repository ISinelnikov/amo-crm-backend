package oss.backend.domain.roistat;

import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nullable;
import com.fasterxml.jackson.annotation.JsonProperty;

public record CallWrapperDto(Collection<CallDto> data) {
    public CallWrapperDto(@JsonProperty("data") @Nullable Collection<CallDto> data) {
        this.data = data == null ? Collections.emptyList() : data;
    }
}
