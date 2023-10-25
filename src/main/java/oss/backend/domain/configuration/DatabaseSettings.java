package oss.backend.domain.configuration;

import oss.backend.util.MappingUtils;

import org.springframework.util.StringUtils;

public class DatabaseSettings implements Configuration {
    private String url;
    private String username;
    private String password;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean isValidConfiguration() {
        return StringUtils.hasText(url)
                && StringUtils.hasText(username)
                && StringUtils.hasText(password);
    }

    @Override
    public String toString() {
        return MappingUtils.convertObjectToJson(this);
    }
}
