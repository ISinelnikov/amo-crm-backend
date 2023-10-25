package oss.backend.domain.dashboard;

import static java.util.Objects.requireNonNull;

public record PipelineStatusInfoItem(long orderId, long statusId, String name, int leads) {
    public PipelineStatusInfoItem(long orderId, long statusId, String name, int leads) {
        this.orderId = orderId;
        this.statusId = statusId;
        this.name = requireNonNull(name, "name can't be null.");
        this.leads = leads;
    }
}
