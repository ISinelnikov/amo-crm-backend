package oss.backend.domain.dashboard;

import java.util.Collection;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

public record LeadsItemsWrapper(Collection<LeadSourceItem> all,
                                Collection<LeadSourceItem> qualified,
                                Collection<LeadSourceItem> closed) {
    public LeadsItemsWrapper(
            Collection<LeadSourceItem> all,
            Collection<LeadSourceItem> qualified,
            Collection<LeadSourceItem> closed
    ) {
        this.all = requireNonNull(all);
        this.qualified = requireNonNull(qualified);
        this.closed = requireNonNull(closed);
    }

    public static LeadsItemsWrapper empty() {
        return new LeadsItemsWrapper(emptyList(), emptyList(), emptyList());
    }

    public static LeadsItemsWrapper of(
            Collection<LeadSourceItem> allItems,
            Collection<LeadSourceItem> qualificationItems,
            Collection<LeadSourceItem> closedItems
    ) {
        return new LeadsItemsWrapper(allItems, qualificationItems, closedItems);
    }
}
