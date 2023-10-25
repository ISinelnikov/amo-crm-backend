package oss.newamo.domain.auth;

public enum OAuthGrandType {
    AUTHORIZATION_CODE("authorization_code"),
    REFRESH_TOKEN("refresh_token");

    private final String value;

    OAuthGrandType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
