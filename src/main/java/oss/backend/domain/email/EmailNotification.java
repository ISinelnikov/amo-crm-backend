package oss.backend.domain.email;

import oss.backend.util.MappingUtils;

import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

public record EmailNotification(
        Set<String> recipients, String subject,
        @Nullable String text, @Nullable EmailTemplate template,
        Map<String, String> params
) {
    @Override
    public String toString() {
        return MappingUtils.convertObjectToJson(this);
    }

    public static EmailNotification ofText(Set<String> recipients, String subject,
            String text,
            Map<String, String> params) {
        return new EmailNotification(recipients, subject, text, null, params);
    }

    public static EmailNotification ofTemplate(Set<String> recipients, String subject,
            EmailTemplate template,
            Map<String, String> params) {
        return new EmailNotification(recipients, subject, null, template, params);
    }
}