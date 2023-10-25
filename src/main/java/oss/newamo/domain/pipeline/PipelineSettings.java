package oss.newamo.domain.pipeline;

import javax.annotation.Nullable;

public record PipelineSettings(
        long id, String name, long orderId, boolean hidden, @Nullable String alias
) {
}
