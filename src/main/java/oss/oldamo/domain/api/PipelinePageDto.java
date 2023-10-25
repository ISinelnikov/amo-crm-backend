package oss.oldamo.domain.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import static java.util.Objects.requireNonNull;

public record PipelinePageDto(Embedded embedded) {
    @JsonCreator
    public PipelinePageDto(@JsonProperty("_embedded") Embedded embedded) {
        this.embedded = requireNonNull(embedded, "embedded can't be null.");
    }

    public record Embedded(List<PipelineDto> pipelines) {
        @JsonCreator
        public Embedded(@JsonProperty("pipelines") List<PipelineDto> pipelines) {
            this.pipelines = requireNonNull(pipelines, "pipelines can't be null.");
        }
    }

    public record PipelineDto(
            long id, String name, long sort,
            boolean main, boolean unsortedOn, boolean archive,
            long accountId, EmbeddedStatus embeddedStatus
    ) {
        @JsonCreator
        public PipelineDto(
                @JsonProperty("id") long id, @JsonProperty("name") String name, @JsonProperty("sort") long sort,
                @JsonProperty("is_main") boolean main, @JsonProperty("is_unsorted_on") boolean unsortedOn,
                @JsonProperty("is_archive") boolean archive, @JsonProperty("account_id") long accountId,
                @JsonProperty("_embedded") EmbeddedStatus embeddedStatus
        ) {
            this.id = id;
            this.name = requireNonNull(name, "name can't be null.");
            this.sort = sort;
            this.main = main;
            this.unsortedOn = unsortedOn;
            this.archive = archive;
            this.accountId = accountId;
            this.embeddedStatus = requireNonNull(embeddedStatus);
        }
    }

    public record EmbeddedStatus(List<StatusDto> statuses) {
        @JsonCreator
        public EmbeddedStatus(@JsonProperty("statuses") List<StatusDto> statuses) {
            this.statuses = requireNonNull(statuses, "statuses can't be null.");
        }
    }

    public record StatusDto(long id, String name, long sort) {
        @JsonCreator
        public StatusDto(@JsonProperty("id") long id, @JsonProperty("name") String name, @JsonProperty("sort") long sort) {
            this.id = id;
            this.name = requireNonNull(name, "name can't be null.");
            this.sort = sort;
        }
    }
}
