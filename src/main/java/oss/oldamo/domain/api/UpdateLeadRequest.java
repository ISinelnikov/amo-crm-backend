package oss.oldamo.domain.api;

import oss.oldamo.domain.api.common.CustomField;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class UpdateLeadRequest extends Request {
    private UpdateLeadRequest(@Nullable Collection<CustomField> customFields,
                             @Nullable Map<EmbeddedType, Collection<Request>> embeddedValues) {
        super(customFields, embeddedValues);
    }

    @Override
    public String toString() {
        return "UpdateLeadRequest{}";
    }

    public static UpdateLeadRequest of(CustomField customField) {
        return new UpdateLeadRequest(List.of(customField), null);
    }
}
