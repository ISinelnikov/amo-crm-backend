package oss.backend.domain.dashboard;

import static java.util.Objects.requireNonNull;

public record LeadsKpiDetailsDay(
        String name, long leads, long leadsRequired, long qualifiedLeads, long qualifiedLeadsRequired
) {
    public LeadsKpiDetailsDay(String name, long leads, long leadsRequired, long qualifiedLeads, long qualifiedLeadsRequired) {
        this.name = requireNonNull(name, "name can't be null.");
        this.leads = leads;
        this.leadsRequired = leadsRequired;
        this.qualifiedLeads = qualifiedLeads;
        this.qualifiedLeadsRequired = qualifiedLeadsRequired;
    }

    public static LeadsKpiDetailsDay of(String name, long leads, long leadsRequired, long qualifiedLeads, long qualifiedLeadsRequired) {
        return new LeadsKpiDetailsDay(name, leads, leadsRequired, qualifiedLeads, qualifiedLeadsRequired);
    }
}
