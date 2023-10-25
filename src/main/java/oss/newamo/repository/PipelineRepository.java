package oss.newamo.repository;

import oss.newamo.domain.pipeline.Pipeline;
import oss.newamo.domain.pipeline.AmoCrmPipeline;
import oss.newamo.domain.pipeline.PipelineSettings;
import oss.newamo.domain.pipeline.status.AmoCrmPipelineStatus;
import oss.newamo.domain.pipeline.status.PipelineStatus;

import java.util.Collection;
import javax.annotation.Nullable;

public interface PipelineRepository {
    boolean isExistPipeline(String clientId, long pipelineId);

    boolean isExistPipelineStatus(String clientId, long pipelineId, long statusId);

    Collection<PipelineSettings> getPipelinesSettings(String clientId);

    Collection<Pipeline> getPipelines(String clientId);

    Collection<PipelineStatus> getPipelineStatuses(String clientId, long pipelineId);

    void savePipelines(String clientId, Collection<AmoCrmPipeline> pipelines);

    void savePipelineStatuses(String clientId, Collection<AmoCrmPipelineStatus> amoCrmPipelineStatuses);

    void removePipelineStatuses(String clientId, long pipelineId);

    void updatePipelineVisible(String clientId, long pipelineId, boolean hidden);

    void updatePipelineAlias(String clientId, long pipelineId, @Nullable String alias);

    void updatePipelineStatusAlias(String clientId, long pipelineId, long statusId, @Nullable String alias);
}
