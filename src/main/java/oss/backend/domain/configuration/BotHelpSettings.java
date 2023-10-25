package oss.backend.domain.configuration;

import oss.backend.util.MappingUtils;

import org.springframework.util.StringUtils;

public class BotHelpSettings implements Configuration {
    private String addLeadBotToken;

    public BotHelpSettings() {
    }

    public String getAddLeadBotToken() {
        return addLeadBotToken;
    }

    public void setAddLeadBotToken(String addLeadBotToken) {
        this.addLeadBotToken = addLeadBotToken;
    }

    @Override
    public boolean isValidConfiguration() {
        return StringUtils.hasText(addLeadBotToken);
    }

    @Override
    public String toString() {
        return MappingUtils.convertObjectToJson(this);
    }
}
