package oss.oldamo.domain.api;

import oss.oldamo.domain.api.common.CustomField;
import oss.oldamo.domain.api.common.Links;
import oss.backend.util.DateUtils;
import oss.backend.util.MappingUtils;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Objects.requireNonNull;

public class LeadModel {
    private final long id;
    private final String name;
    @Nullable
    private final Long pipelineId;
    @Nullable
    private final Long statusId;

    private final boolean deleted;
    @Nullable
    private final Long accountId;

    @Nullable
    private final Long updatedBy;

    private final ZonedDateTime createdDate;
    @Nullable
    private final ZonedDateTime updatedDate;
    @Nullable
    private final ZonedDateTime closedDate;

    private final Links links;
    private final Map<Long, CustomField> fields;

    private final LeadEmbeddedDto embedded;

    @JsonCreator
    public LeadModel(
            @JsonProperty("id") long id,
            @JsonProperty("name") String name,
            @JsonProperty("status_id") @Nullable Long statusId,
            @JsonProperty("pipeline_id") @Nullable Long pipelineId,
            @JsonProperty("deleted") boolean deleted,
            @JsonProperty("created_at") long createdAt,
            @JsonProperty("updated_by") @Nullable Long updatedBy,
            @JsonProperty("updated_at") @Nullable Long updatedAt,
            @JsonProperty("closed_at") @Nullable Long closedAt,
            @JsonProperty("account_id") @Nullable Long accountId,
            @JsonProperty("_links") Links links,
            @JsonProperty("custom_fields_values") Collection<CustomField> fields,
            @JsonProperty("_embedded") LeadEmbeddedDto embedded
    ) {
        this.id = id;
        this.name = name;
        this.pipelineId = pipelineId;
        this.statusId = statusId;
        this.deleted = deleted;
        this.createdDate = DateUtils.ofUnixTimestamp(createdAt);
        this.updatedBy = updatedBy;
        this.updatedDate = updatedAt == null ? null : DateUtils.ofUnixTimestamp(updatedAt);
        this.closedDate = closedAt == null ? null : DateUtils.ofUnixTimestamp(closedAt);
        this.accountId = accountId;
        this.links = requireNonNull(links, "Links can't be null.");
        this.fields = fields == null ? Collections.emptyMap() : fields
                .stream()
                .collect(Collectors.toMap(CustomField::id, Function.identity()));

        this.embedded = embedded;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public Long getPipelineId() {
        return pipelineId;
    }

    @Nullable
    public Long getStatusId() {
        return statusId;
    }

    public boolean isDeleted() {
        return deleted;
    }

    @Nullable
    public Long getAccountId() {
        return accountId;
    }

    @Nullable
    public Long getUpdatedBy() {
        return updatedBy;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    @Nullable
    public ZonedDateTime getUpdatedDate() {
        return updatedDate;
    }

    @Nullable
    public ZonedDateTime getClosedDate() {
        return closedDate;
    }

    public Links getLinks() {
        return links;
    }

    public Map<Long, CustomField> getFields() {
        return fields;
    }

    public Collection<LeadEmbeddedContact> getContacts() {
        if (embedded == null || embedded.contacts() == null) {
            return Collections.emptyList();
        }
        return embedded.contacts();
    }

    @Override
    public String toString() {
        return MappingUtils.convertObjectToJson(this);
    }

    public record LeadEmbeddedDto(@JsonProperty("contacts") Collection<LeadEmbeddedContact> contacts) {
        @Override
        public String toString() {
            return MappingUtils.convertObjectToJson(this);
        }
    }

    public record LeadEmbeddedContact(@JsonProperty("id") long id, @JsonProperty("is_main") boolean main) {
        @Override
        public String toString() {
            return MappingUtils.convertObjectToJson(this);
        }
    }
}
