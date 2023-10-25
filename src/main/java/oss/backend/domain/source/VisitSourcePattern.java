package oss.backend.domain.source;

import oss.backend.util.OSSStringUtils;
import oss.backend.util.MappingUtils;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public final class VisitSourcePattern {
    private final long id;
    private final long groupId;
    private final int priority;
    private final String groupName;
    private final List<VisitPatternItem> items;

    private final int selectId;
    private final int selectOptionId;

    public VisitSourcePattern(long id, long groupId, int priority, String groupName, Collection<VisitPatternItem> markerDetails, int selectId, int selectOptionId) {
        this.id = id;
        this.groupId = groupId;
        this.priority = priority;
        this.groupName = requireNonNull(groupName, "name can't be null.");
        this.items = requireNonNull(markerDetails, "markerDetails can't be null.")
                .stream()
                .sorted(Comparator.comparingInt(VisitPatternItem::orderId))
                .collect(Collectors.toList());
        this.selectId = selectId;
        this.selectOptionId = selectOptionId;
    }

    public long getId() {
        return id;
    }

    public long getGroupId() {
        return groupId;
    }

    public int getPriority() {
        return priority;
    }

    public String getGroupName() {
        return groupName;
    }

    public List<VisitPatternItem> getItems() {
        return items;
    }

    public int getSelectId() {
        return selectId;
    }

    public int getSelectOptionId() {
        return selectOptionId;
    }

    public boolean matchVisit(List<String> displayNameByLevel) {
        if (displayNameByLevel.size() < items.size()) {
            return false;
        }
        boolean matches = false;
        for (int idx = 0; idx < items.size(); idx++) {
            VisitPatternItem details = items.get(idx);
            String value = OSSStringUtils.valueToNull(displayNameByLevel.get(idx));
            if (value != null) {
                matches = details.matches(value);
            } else {
                matches = false;
            }
        }

        return matches;
    }

    @Override
    public String toString() {
        return MappingUtils.convertObjectToJson(this);
    }
}
