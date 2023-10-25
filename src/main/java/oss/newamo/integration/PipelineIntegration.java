package oss.newamo.integration;

import oss.backend.util.HttpUtils;
import oss.backend.util.MappingUtils;
import oss.newamo.annotation.Integration;
import oss.newamo.cache.ClientCredentialsCache;
import oss.newamo.domain.pipeline.AmoCrmPipeline;
import oss.newamo.domain.pipeline.status.AmoCrmPipelineStatus;

import java.util.Collection;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Objects.requireNonNull;

@Integration
public class PipelineIntegration {
    private static final Logger logger = LoggerFactory.getLogger(PipelineIntegration.class);

    private static final String API_PATH_PIPELINE = "/api/v4/leads/pipelines";

    private static final String API_PATH_PIPELINE_STATUSES = "/api/v4/leads/pipelines/{pipeline_id}/statuses";

    private final ClientCredentialsCache clientCredentialsCache;

    public PipelineIntegration(ClientCredentialsCache clientCredentialsCache) {
        this.clientCredentialsCache = clientCredentialsCache;
    }

    public void loadAmoCrmPipelinesAsync(String clientId, Consumer<Collection<AmoCrmPipeline>> promise) {
        clientCredentialsCache.getClientCredentials(clientId).ifPresent(credentials -> {
            String preparedUrl = credentials.getAmoCrmPath() + API_PATH_PIPELINE;
            HttpHeaders headers = HttpUtils.getBearerHeaders(credentials.getAccessToken());

            ResponseEntity<String> response = HttpUtils.jsonGetRequest(preparedUrl, headers);

            logger.debug("Invoke url: {}, result code: {}, body: {}.", preparedUrl, response.getStatusCode(), response.getBody());
            if (response.getStatusCode().is2xxSuccessful()) {
                PipelinePage pipelinePage = MappingUtils.parseJsonToInstance(response.getBody(), PipelinePage.class);
                if (pipelinePage != null) {
                    promise.accept(pipelinePage.result());
                }
            }
        });
    }

    public void loadAmoCrmPipelineStatusesAsync(String clientId, long pipelineId, Consumer<Collection<AmoCrmPipelineStatus>> promise) {
        clientCredentialsCache.getClientCredentials(clientId).ifPresent(credentials -> {
            String preparedUrl = credentials.getAmoCrmPath() + API_PATH_PIPELINE_STATUSES
                    .replace("{pipeline_id}", String.valueOf(pipelineId));
            HttpHeaders headers = HttpUtils.getBearerHeaders(credentials.getAccessToken());

            ResponseEntity<String> response = HttpUtils.jsonGetRequest(preparedUrl, headers);

            logger.debug("Invoke url: {}, result code: {}, body: {}.", preparedUrl, response.getStatusCode(), response.getBody());
            if (response.getStatusCode().is2xxSuccessful()) {
                PipelineStatusPage pipelineStatusPage = MappingUtils.parseJsonToInstance(response.getBody(), PipelineStatusPage.class);
                if (pipelineStatusPage != null) {
                    promise.accept(pipelineStatusPage.result());
                }
            }
        });
    }

    public record PipelinePage(Embedded embedded) {
        @JsonCreator
        public PipelinePage(@JsonProperty("_embedded") Embedded embedded) {
            this.embedded = requireNonNull(embedded, "embedded can't be null.");
        }

        public record Embedded(Collection<AmoCrmPipeline> pipelines) {
            @JsonCreator
            public Embedded(@JsonProperty("pipelines") Collection<AmoCrmPipeline> pipelines) {
                this.pipelines = requireNonNull(pipelines, "pipelines can't be null.");
            }
        }

        public Collection<AmoCrmPipeline> result() {
            return embedded().pipelines();
        }
    }

    public record PipelineStatusPage(Embedded embedded) {
        @JsonCreator
        public PipelineStatusPage(@JsonProperty("_embedded") Embedded embedded) {
            this.embedded = requireNonNull(embedded, "embedded can't be null.");
        }

        public record Embedded(Collection<AmoCrmPipelineStatus> pipelineStatuses) {
            @JsonCreator
            public Embedded(@JsonProperty("statuses") Collection<AmoCrmPipelineStatus> pipelineStatuses) {
                this.pipelineStatuses = requireNonNull(pipelineStatuses, "pipelineStatuses can't be null.");
            }
        }

        public Collection<AmoCrmPipelineStatus> result() {
            return embedded().pipelineStatuses();
        }
    }
}
