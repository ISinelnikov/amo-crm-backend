package oss.backend.domain.space;

import java.time.ZonedDateTime;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public record SpaceSettings(
        long id, String spaceName, @Nullable String domain,
        ZonedDateTime creationDate, @Nullable ZonedDateTime confirmationDate,
        @Nullable String amoClientId
) {
    public SpaceSettings(
            long id, String spaceName, @Nullable String domain,
            ZonedDateTime creationDate, @Nullable ZonedDateTime confirmationDate,
            @Nullable String amoClientId
    ) {
        this.id = id;
        this.spaceName = requireNonNull(spaceName, "spaceName can't be null.");
        this.domain = domain;
        this.confirmationDate = confirmationDate;
        this.creationDate = requireNonNull(creationDate, "creationDate can't be null.");
        this.amoClientId = amoClientId;
    }
}
