package oss.backend.domain.dashboard;

import javax.annotation.Nullable;

public record LeadSourceItem(String name, int value) {
    private static final String UNDEFINED = "Без источника";

    public LeadSourceItem(@Nullable String name, int value) {
        this.name = name == null ? UNDEFINED : name;
        this.value = value;
    }
}
