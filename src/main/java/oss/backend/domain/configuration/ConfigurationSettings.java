package oss.backend.domain.configuration;

import oss.backend.util.MappingUtils;

import org.springframework.util.StringUtils;

public class ConfigurationSettings implements Configuration {
    private String redirectUri;
    private String amoCrmPath;
    private String roiStatProjectId;
    private String roiStatApiKey;
    private String notificationServicePath;

    private DatabaseSettings database = new DatabaseSettings();
    private BotHelpSettings botHelp = new BotHelpSettings();

    public ConfigurationSettings() {
    }

    public DatabaseSettings getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseSettings database) {
        this.database = database;
    }

    public BotHelpSettings getBotHelp() {
        return botHelp;
    }

    public void setBotHelp(BotHelpSettings botHelp) {
        this.botHelp = botHelp;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getAmoCrmPath() {
        return amoCrmPath;
    }

    public void setAmoCrmPath(String amoCrmPath) {
        this.amoCrmPath = amoCrmPath;
    }

    public String getRoiStatProjectId() {
        return roiStatProjectId;
    }

    public void setRoiStatProjectId(String roiStatProjectId) {
        this.roiStatProjectId = roiStatProjectId;
    }

    public String getRoiStatApiKey() {
        return roiStatApiKey;
    }

    public void setRoiStatApiKey(String roiStatApiKey) {
        this.roiStatApiKey = roiStatApiKey;
    }

    public String getNotificationServicePath() {
        return notificationServicePath;
    }

    public void setNotificationServicePath(String notificationServicePath) {
        this.notificationServicePath = notificationServicePath;
    }

    public boolean isValid() {
        return database.isValidConfiguration()
                && botHelp.isValidConfiguration()
                && StringUtils.hasText(redirectUri)
                && StringUtils.hasText(amoCrmPath)
                && StringUtils.hasText(roiStatProjectId)
                && StringUtils.hasText(roiStatApiKey)
                && StringUtils.hasText(notificationServicePath);
    }

    @Override
    public boolean isValidConfiguration() {
        return false;
    }

    @Override
    public String toString() {
        return MappingUtils.convertObjectToJson(this);
    }
}

