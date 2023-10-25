package oss.backend.service;

import oss.backend.domain.email.EmailNotification;
import oss.backend.domain.email.EmailTemplate;
import oss.backend.util.HttpUtils;

import java.util.Collections;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final String notificationServicePath;

    public EmailService(ConfigurationService configurationService) {
        this.notificationServicePath = configurationService.getApplicationSettings().getNotificationServicePath();
    }

    public void sendConfirmationEmail(String email) {
        ResponseEntity<String> response = HttpUtils.jsonPostRequest(notificationServicePath,
                HttpUtils.addApiKeyHeaders(HttpUtils.EMPTY_HEADERS, "test"),
                EmailNotification.ofTemplate(
                        Set.of(email),
                        "Регистрация",
                        EmailTemplate.ACCOUNT_CREATED,
                        Collections.emptyMap()
                )
        );

        logger.debug("Invoke url: {}, result: {}, body: {}.",notificationServicePath, response.getStatusCode(), response.getBody());
    }
}
