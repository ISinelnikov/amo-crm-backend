package oss.backend.domain.dashboard;

import oss.backend.util.MappingUtils;

public record LeadsKpiDetails(long leads, long leadsRequired, long qualifiedLeads, long qualifiedLeadsRequired) {
    public static LeadsKpiDetails empty() {
        return new LeadsKpiDetails(0, 0, 0, 0);
    }

    @Override
    public String toString() {
        return MappingUtils.convertObjectToJson(this);
    }
}
