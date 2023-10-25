package oss.newamo.domain.event;

import org.springframework.context.ApplicationEvent;

public class AddedClientCredentials extends ApplicationEvent {
    private final String clientId;

    public AddedClientCredentials(Object source, String clientId) {
        super(source);
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }
}
