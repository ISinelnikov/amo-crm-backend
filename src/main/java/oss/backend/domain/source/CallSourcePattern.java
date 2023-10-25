package oss.backend.domain.source;

import static java.util.Objects.requireNonNull;

public record CallSourcePattern(long patternId, long groupId, String groupName, String countryCode, String phoneNumber) {
    public CallSourcePattern(long patternId, long groupId, String groupName, String countryCode, String phoneNumber) {
        this.patternId = patternId;
        this.groupId = groupId;
        this.groupName = groupName;
        this.countryCode = requireNonNull(countryCode, "countryCode can't be null.");
        this.phoneNumber = requireNonNull(phoneNumber, "phone can't be null.");
    }

    public String fullNumber() {
        return countryCode + phoneNumber;
    }
}
