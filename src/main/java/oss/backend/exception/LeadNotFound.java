package oss.backend.exception;

import java.io.Serial;

public class LeadNotFound extends Throwable {
    @Serial
    private static final long serialVersionUID = 5395652527582309026L;

    public LeadNotFound(String message) {
        super(message);
    }

    public static LeadNotFound of(long leadId) {
        return new LeadNotFound("Not found lead: " + leadId);
    }
}
