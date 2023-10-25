package oss.backend.domain.source;

import oss.backend.util.MappingUtils;

import static java.util.Objects.requireNonNull;

public record GroupInfo(long groupId, String groupName, long selectId, long optionId) {
    public GroupInfo(long groupId, String groupName, long selectId, long optionId) {
        this.groupId = groupId;
        this.groupName = requireNonNull(groupName, "groupName can't be null.");
        this.selectId = selectId;
        this.optionId = optionId;
    }

    @Override
    public String toString() {
        return MappingUtils.convertObjectToJson(this);
    }
}
