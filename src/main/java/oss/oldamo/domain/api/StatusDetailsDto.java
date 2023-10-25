package oss.oldamo.domain.api;

import oss.backend.util.DateUtils;
import oss.backend.util.MappingUtils;

import java.time.ZonedDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Objects.requireNonNull;

public record StatusDetailsDto(Embedded embedded) {
    @JsonCreator
    public StatusDetailsDto(@JsonProperty("_embedded") Embedded embedded) {
        this.embedded = requireNonNull(embedded, "embedded can't be null.");
    }

    public record Embedded(List<LeadEventDto> events) {
        @JsonCreator
        public Embedded(@JsonProperty("events") List<LeadEventDto> events) {
            this.events = requireNonNull(events, "pipelines can't be null.");
        }
    }

    public static final class LeadEventDto {
        private final String id;
        private final String type;
        private final long leadId;
        private final long userId;
        private final ZonedDateTime dateCreate;
        private final List<LeadStatusWrapper> before;
        private final List<LeadStatusWrapper> after;

        @JsonCreator
        public LeadEventDto(
                @JsonProperty("id") String id,
                @JsonProperty("type") String type,
                @JsonProperty("entity_id") long leadId,
                @JsonProperty("created_by") long userId,
                @JsonProperty("created_at") long createdAt,
                @JsonProperty("value_before") List<LeadStatusWrapper> before,
                @JsonProperty("value_after") List<LeadStatusWrapper> after
        ) {
            this.id = id;
            this.type = requireNonNull(type, "name can't be null.");
            this.leadId = leadId;
            this.userId = userId;
            this.dateCreate = DateUtils.ofUnixTimestamp(createdAt);
            this.before = before;
            this.after = after;
        }

        public String getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public long getLeadId() {
            return leadId;
        }

        public long getUserId() {
            return userId;
        }

        public ZonedDateTime getDateCreate() {
            return dateCreate;
        }

        public List<LeadStatusWrapper> getBefore() {
            return before;
        }

        public List<LeadStatusWrapper> getAfter() {
            return after;
        }

        @Override
        public String toString() {
            return MappingUtils.convertObjectToJson(this);
        }
    }

    public record LeadStatusWrapper(@JsonProperty("lead_status") LeadStatus leadStatus) {
    }

    public record LeadStatus(@JsonProperty("id") long id, @JsonProperty("pipeline_id") long pipelineId) {
    }
}