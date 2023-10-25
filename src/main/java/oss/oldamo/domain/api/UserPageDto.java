package oss.oldamo.domain.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import static java.util.Objects.requireNonNull;

public record UserPageDto(Embedded embedded) {
    @JsonCreator
    public UserPageDto(@JsonProperty("_embedded") Embedded embedded) {
        this.embedded = requireNonNull(embedded, "embedded can't be null.");
    }

    public record Embedded(List<UserDto> users) {
        @JsonCreator
        public Embedded(@JsonProperty("details") List<UserDto> users) {
            this.users = requireNonNull(users, "pipelines can't be null.");
        }
    }

    public record UserDto(
            long id, String name, String email
    ) {
        @JsonCreator
        public UserDto(
                @JsonProperty("id") long id, @JsonProperty("name") String name, @JsonProperty("email") String email
        ) {
            this.id = id;
            this.name = requireNonNull(name, "name can't be null.");
            this.email = email;
        }
    }
}
