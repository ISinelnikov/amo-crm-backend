package oss.backend.domain.dashboard;

import static java.util.Objects.requireNonNull;

public record ManagerDealsInfo(String name, int completedMeets, int preparedMeets, int deals, double profit) {
    public ManagerDealsInfo(String name, int completedMeets, int preparedMeets, int deals, double profit) {
        this.name = requireNonNull(name, "name can't be null.");
        this.completedMeets = completedMeets;
        this.preparedMeets = preparedMeets;
        this.deals = deals;
        this.profit = profit;
    }
}
