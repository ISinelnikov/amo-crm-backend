package oss.backend.domain.canvas;

public class Element<T> {
    private final String id;
    private final String name;
    private final T settings;

    public Element(String id, String name, T settings) {
        this.id = id;
        this.name = name;
        this.settings = settings;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public T getSettings() {
        return settings;
    }

    public enum ElementRange {
        INTERVAL,
        MONTH
    }

    public enum ElementTemplate {
        PIE_CHART,
        BAR_CHART_VERTICAL,
        BAR_CHART_HORIZONTAL
    }

    public enum ElementType {
        CHART,
        TABLE
    }

    public enum ElementSize {
        SMALL,
        MEDIUM,
        LARGE
    }
}
