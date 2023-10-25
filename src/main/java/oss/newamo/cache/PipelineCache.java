package oss.newamo.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import oss.backend.util.MappingUtils;
import oss.newamo.annotation.Cache;
import oss.newamo.domain.pipeline.Pipeline;
import oss.newamo.domain.pipeline.status.PipelineStatus;
import oss.newamo.repository.PipelineRepository;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;
import static oss.backend.configuration.ThreadScopeConfiguration.DEFAULT_EXECUTOR;

@Cache
public class PipelineCache {
    private static final Logger logger = LoggerFactory.getLogger(PipelineCache.class);

    private static final long PIPELINE_CACHE_EXPIRE_AFTER_WRITE = Long.getLong("pipeline.cache.expire.after.write.min", 10L);

    private final LoadingCache<String, Collection<Pipeline>> pipelinesCache;
    private final LoadingCache<PipelineStatusCacheKey, Collection<PipelineStatus>> pipelineStatusesCache;

    public PipelineCache(PipelineRepository pipelineRepository, @Qualifier(DEFAULT_EXECUTOR) ExecutorService executor) {
        this.pipelinesCache = Caffeine.newBuilder()
                .executor(executor)
                .expireAfterWrite(PIPELINE_CACHE_EXPIRE_AFTER_WRITE, TimeUnit.MINUTES)
                .build(clientId -> {
                    Collection<Pipeline> pipelines = pipelineRepository.getPipelines(clientId);
                    logger.debug("Load pipelines: {} by clientId: {}.", pipelines, clientId);
                    return pipelines;
                });

        this.pipelineStatusesCache = Caffeine.newBuilder()
                .executor(executor)
                .expireAfterWrite(PIPELINE_CACHE_EXPIRE_AFTER_WRITE, TimeUnit.MINUTES)
                .build(key -> {
                    Collection<PipelineStatus> pipelineStatuses = pipelineRepository.getPipelineStatuses(key.clientId(), key.pipelineId());
                    logger.debug("Load pipelineStatuses: {} by key: {}.", pipelineStatuses, key);
                    return pipelineStatuses;
                });
    }

    public Collection<Pipeline> getPipelines(String clientId) {
        return pipelinesCache.get(clientId);
    }

    @Nullable
    public Pipeline getPipeline(String clientId, long id) {
        return pipelinesCache.get(clientId)
                .stream()
                .filter(pipeline -> pipeline.id() == id)
                .findFirst()
                .orElse(null);
    }

    public Collection<PipelineStatus> getPipelineStatuses(String clientId, long pipelineId) {
        return pipelineStatusesCache.get(PipelineStatusCacheKey.of(clientId, pipelineId));
    }

    @Nullable
    public PipelineStatus getPipelineStatuses(String clientId, long pipelineId, long id) {
        return pipelineStatusesCache.get(PipelineStatusCacheKey.of(clientId, pipelineId))
                .stream()
                .filter(pipelineStatus -> pipelineStatus.id() == id)
                .findFirst()
                .orElse(null);
    }

    public void invalidatePipelinesCache(String clientId) {
        pipelinesCache.invalidate(clientId);
    }

    public void invalidatePipelineStatusesCache(String clientId, long pipelineId) {
        pipelineStatusesCache.invalidate(PipelineStatusCacheKey.of(clientId, pipelineId));
    }

    public void invalidateAllCaches(String clientId, long pipelineId) {
        invalidatePipelinesCache(clientId);
        invalidatePipelineStatusesCache(clientId, pipelineId);
    }

    public record PipelineStatusCacheKey(String clientId, long pipelineId) {
        public PipelineStatusCacheKey(String clientId, long pipelineId) {
            this.clientId = requireNonNull(clientId, "clientId can't be null.");
            this.pipelineId = pipelineId;
        }

        @Override
        public String toString() {
            return MappingUtils.convertObjectToJson(this);
        }

        public static PipelineStatusCacheKey of(String clientId, long pipelineId) {
            return new PipelineStatusCacheKey(clientId, pipelineId);
        }
    }
}
