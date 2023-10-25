package oss.oldamo.domain.api;

import oss.oldamo.domain.api.common.CustomField;
import oss.backend.util.OSSStringUtils;

import java.util.Collection;
import javax.annotation.Nullable;

public class CreateContactRequest extends Request {
    @Nullable
    private final String name;

    public CreateContactRequest(@Nullable String name, @Nullable Collection<CustomField> customFields) {
        super(customFields, null);
        this.name = OSSStringUtils.valueToNull(name);
    }

    @Nullable
    public String getName() {
        return name;
    }
}
