package oss.bot;

import java.util.stream.Stream;

public record BotUserDetails(long userId, String name, String phone, String referralCode, UserType type, boolean source1091617) {
    public enum UserType {
        INTERNAL,
        REFERRER;

        public static UserType of(String value) {
            return Stream.of(values())
                    .filter(userType -> userType.name().equals(value))
                    .findFirst()
                    .orElse(REFERRER);
        }
    }
}
