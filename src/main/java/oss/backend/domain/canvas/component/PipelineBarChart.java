package oss.backend.domain.canvas.component;

public class PipelineBarChart implements Template {
    private final TemplateSize size;

    public PipelineBarChart(TemplateSize size) {
        this.size = size;
    }

    @Override
    public TemplateType getTemplateType() {
        return TemplateType.BAR_CHART_VERTICAL;
    }

    @Override
    public TemplateSize getTemplateSize() {
        return size;
    }
}
