package oss.newamo.service;

import oss.newamo.cache.PipelineCache;
import oss.newamo.domain.event.AddedClientCredentials;
import oss.newamo.domain.pipeline.AmoCrmPipeline;
import oss.newamo.domain.pipeline.Pipeline;
import oss.newamo.domain.pipeline.status.AmoCrmPipelineStatus;
import oss.newamo.domain.pipeline.status.PipelineStatus;
import oss.newamo.domain.pipeline.PipelineSettings;
import oss.newamo.integration.PipelineIntegration;
import oss.newamo.repository.PipelineRepository;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class PipelineService implements ApplicationListener<AddedClientCredentials> {
    private final PipelineCache pipelineCache;
    private final PipelineRepository pipelineRepository;
    private final PipelineIntegration pipelineIntegration;

    public PipelineService(
            PipelineCache pipelineCache, PipelineRepository pipelineRepository, PipelineIntegration pipelineIntegration
    ) {
        this.pipelineCache = pipelineCache;
        this.pipelineRepository = pipelineRepository;
        this.pipelineIntegration = pipelineIntegration;

        //onApplicationEvent(new AddedClientCredentials(this, "a734bcaf-419e-4bcb-a102-aad3557c3e70"));
    }

    public Collection<PipelineSettings> getPipelinesSettings(String clientId) {
        return pipelineRepository.getPipelinesSettings(clientId);
    }

    public Collection<Pipeline> getPipelines(String clientId) {
        return pipelineCache.getPipelines(clientId);
    }

    @Nullable
    public Pipeline getPipeline(String clientId, long id) {
        return pipelineCache.getPipeline(clientId, id);
    }

    public Collection<PipelineStatus> getPipelineStatuses(String clientId, long pipelineId) {
        return pipelineCache.getPipelineStatuses(clientId, pipelineId);
    }

    @Nullable
    public PipelineStatus getPipelineStatus(String clientId, long pipelineId, long id) {
        return pipelineCache.getPipelineStatuses(clientId, pipelineId, id);
    }

    public void updatePipelineAlias(String clientId, long pipelineId, String alias) {
        pipelineRepository.updatePipelineAlias(clientId, pipelineId, alias);
        pipelineCache.invalidatePipelinesCache(clientId);
    }

    public void updatePipelineStatusAlias(String clientId, long pipelineId, long statusId, String alias) {
        pipelineRepository.updatePipelineStatusAlias(clientId, pipelineId, statusId, alias);
        pipelineCache.invalidatePipelineStatusesCache(clientId, pipelineId);
    }

    public void updatePipelineVisible(String clientId, long pipelineId, boolean hidden) {
        pipelineRepository.updatePipelineVisible(clientId, pipelineId, hidden);
    }

    private void refreshPipelineStatuses(String clientId, long pipelineId) {
        pipelineIntegration.loadAmoCrmPipelineStatusesAsync(clientId, pipelineId, amoCrmPipelineStatuses -> {
            List<AmoCrmPipelineStatus> newPipelineStatuses = amoCrmPipelineStatuses.stream()
                    .filter(amoCrmPipelineStatus -> !pipelineRepository.isExistPipelineStatus(clientId, pipelineId,
                            amoCrmPipelineStatus.getId())).toList();
            pipelineRepository.savePipelineStatuses(clientId, newPipelineStatuses);
            pipelineCache.invalidateAllCaches(clientId, pipelineId);
        });
    }

    private void removePipelineStatuses(String clientId, long pipelineId) {
        pipelineRepository.removePipelineStatuses(clientId, pipelineId);
        pipelineCache.invalidateAllCaches(clientId, pipelineId);
    }

    @Override
    public void onApplicationEvent(AddedClientCredentials event) {
        String clientId = event.getClientId();
        pipelineIntegration.loadAmoCrmPipelinesAsync(
                clientId, amoCrmPipelines -> {
                    List<AmoCrmPipeline> newPipelines = amoCrmPipelines.stream()
                            .filter(amoCrmPipeline -> !pipelineRepository.isExistPipeline(clientId, amoCrmPipeline.getId()))
                            .toList();
                    pipelineRepository.savePipelines(clientId, newPipelines);
                    amoCrmPipelines.forEach(amoCrmPipeline -> refreshPipelineStatuses(clientId, amoCrmPipeline.getId()));
                }
        );
    }
}
