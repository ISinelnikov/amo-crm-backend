package oss.bot;

import oss.backend.util.MappingUtils;

import java.util.Collection;

import static java.util.Objects.requireNonNull;

public final class BotReferrer {
    private final long userId;
    private final String name;
    private final String phone;
    private final Collection<ExternalLead> leads;

    private final int allLeads;
    private final long activeLeads;

    public BotReferrer(long userId, String name, String phone, Collection<ExternalLead> leads) {
        this.userId = userId;
        this.name = requireNonNull(name, "name can't be null.");
        this.phone = requireNonNull(phone, "phone can't be null.");
        this.leads = requireNonNull(leads, "leads can't be null.");
        this.allLeads = leads.size();
        this.activeLeads = leads.stream().filter(lead -> lead.statusId() != null && lead.statusId() != 143).count();
    }

    public long userId() {
        return userId;
    }

    public String name() {
        return name;
    }

    public String phone() {
        return phone;
    }

    public Collection<ExternalLead> leads() {
        return leads;
    }

    public int getAllLeads() {
        return allLeads;
    }

    public long getActiveLeads() {
        return activeLeads;
    }

    @Override
    public String toString() {
        return MappingUtils.convertObjectToJson(this);
    }

    public static class Builder {
        private long userId;
        private String name;
        private String phone;
        private Collection<ExternalLead> leads;

        private Builder() {
        }

        public static Builder getInstance() {
            return new Builder();
        }

        public long getUserId() {
            return userId;
        }

        public Builder setUserId(long userId) {
            this.userId = userId;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setPhone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder setExternalLeads(Collection<ExternalLead> leads) {
            this.leads = leads;
            return this;
        }

        public BotReferrer build() {
            return new BotReferrer(userId, name, phone, leads);
        }
    }
}
