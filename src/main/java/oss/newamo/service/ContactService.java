package oss.newamo.service;

import oss.newamo.domain.contact.AmoCrmContact;
import oss.newamo.integration.ContactIntegration;

import javax.annotation.Nullable;
import org.springframework.stereotype.Service;

@Service
public class ContactService {
    private final ContactIntegration contactIntegration;

    public ContactService(ContactIntegration contactIntegration) {
        this.contactIntegration = contactIntegration;
    }

    @Nullable
    public AmoCrmContact getAmoCrmContact(String clientId, long contactId) {
        return contactIntegration.loadAmoCrmContactById(clientId, contactId);
    }
}
