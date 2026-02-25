package madkit.grafana.dashboard;

import java.util.List;

/**
 * Builds a valid Grafana dashboard JSON model from a list of {@link PanelDef}s.
 * <p>
 * This class is a pure function with no side effects — it takes panel definitions
 * and returns a JSON string. This makes it trivially unit-testable.
 */
public class DashboardJsonBuilder {

    private static final int PANEL_WIDTH = 12;
    private static final int PANEL_HEIGHT = 8;
    private static final int COLUMNS = 24;

    /**
     * Builds the full Grafana dashboard JSON payload with the default {@code "2s"} refresh interval.
     *
     * @param title  the dashboard title
     * @param panels the list of panel definitions
     * @return a JSON string ready for {@code POST /api/dashboards/db}
     */
    public String build(String title, List<? extends PanelDef> panels) {
        return build(title, panels, "2s");
    }

    /**
     * Builds the full Grafana dashboard JSON payload with a custom refresh interval.
     *
     * @param title           the dashboard title
     * @param panels          the list of panel definitions
     * @param refreshInterval the dashboard auto-refresh interval (e.g. {@code "1s"}, {@code "5s"})
     * @return a JSON string ready for {@code POST /api/dashboards/db}
     */
    public String build(String title, List<? extends PanelDef> panels, String refreshInterval) {
        var sb = new StringBuilder();
        sb.append('{');
        appendField(sb, "uid", titleToUid(title));
        sb.append(',');
        appendField(sb, "title", title);
        sb.append(',');
        sb.append("\"panels\":[");
        for (int i = 0; i < panels.size(); i++) {
            if (i > 0) sb.append(',');
            switch (panels.get(i)) {
                case PanelDefinition p  -> appendPanel(sb, p, i);
                case XYPanelDefinition p -> appendXYPanel(sb, p, i);
            }
        }
        sb.append(']');
        sb.append(",\"refresh\":\"").append(refreshInterval).append("\"");
        sb.append(",\"time\":{\"from\":\"now-5m\",\"to\":\"now\"}");
        sb.append(",\"timezone\":\"browser\"");
        sb.append(",\"schemaVersion\":39");
        sb.append('}');
        return sb.toString();
    }

    /**
     * Appends a standard panel (time series, stat, or table) to the JSON output.
     *
     * @param sb    the StringBuilder to append to
     * @param panel the panel definition
     * @param index zero-based panel index (used for id and grid position)
     */
    private void appendPanel(StringBuilder sb, PanelDefinition panel, int index) {
        int x = (index % (COLUMNS / PANEL_WIDTH)) * PANEL_WIDTH;
        int y = (index / (COLUMNS / PANEL_WIDTH)) * PANEL_HEIGHT;
        sb.append('{');
        sb.append("\"id\":").append(index + 1).append(',');
        appendField(sb, "title", panel.title());
        sb.append(',');
        appendField(sb, "type", panel.panelType());
        sb.append(',');
        sb.append("\"gridPos\":{\"h\":").append(PANEL_HEIGHT)
          .append(",\"w\":").append(PANEL_WIDTH)
          .append(",\"x\":").append(x)
          .append(",\"y\":").append(y).append("},");
        sb.append("\"targets\":[{");
        appendField(sb, "refId", "A");
        sb.append(',');
        sb.append("\"datasource\":{\"type\":\"influxdb\",\"uid\":\"influxdb-madkit\"},");
        appendField(sb, "query", buildFluxQuery(panel));
        sb.append("}]");
        sb.append('}');
    }

    /**
     * Builds a Flux query for a standard panel that filters on a single field.
     *
     * @param panel the panel definition containing measurement and field
     * @return a JSON-escaped Flux query string
     */
    private String buildFluxQuery(PanelDefinition panel) {
        return "from(bucket: \\\"madkit\\\") "
             + "|> range(start: v.timeRangeStart, stop: v.timeRangeStop) "
             + "|> filter(fn: (r) => r._measurement == \\\"" + panel.measurement() + "\\\") "
             + "|> filter(fn: (r) => r._field == \\\"" + panel.field() + "\\\")";
    }

    /**
     * Appends an XY chart panel to the JSON output, including the
     * {@code fieldConfig}, {@code options} with field matchers, and the pivot query.
     *
     * @param sb    the StringBuilder to append to
     * @param panel the XY panel definition
     * @param index zero-based panel index (used for id and grid position)
     */
    private void appendXYPanel(StringBuilder sb, XYPanelDefinition panel, int index) {
        int x = (index % (COLUMNS / PANEL_WIDTH)) * PANEL_WIDTH;
        int y = (index / (COLUMNS / PANEL_WIDTH)) * PANEL_HEIGHT;
        sb.append('{');
        sb.append("\"id\":").append(index + 1).append(',');
        appendField(sb, "title", panel.title());
        sb.append(',');
        appendField(sb, "type", "xychart");
        sb.append(',');
        sb.append("\"gridPos\":{\"h\":").append(PANEL_HEIGHT)
          .append(",\"w\":").append(PANEL_WIDTH)
          .append(",\"x\":").append(x)
          .append(",\"y\":").append(y).append("},");
        sb.append("\"targets\":[{");
        appendField(sb, "refId", "A");
        sb.append(',');
        sb.append("\"datasource\":{\"type\":\"influxdb\",\"uid\":\"influxdb-madkit\"},");
        appendField(sb, "query", buildXYFluxQuery(panel));
        sb.append("}]");
        sb.append(",\"fieldConfig\":{\"defaults\":{},\"overrides\":[]}");
        sb.append(",\"options\":{\"seriesMapping\":\"manual\",\"series\":[{")
          .append("\"frame\":{\"matcher\":{\"id\":\"byIndex\",\"options\":0}},")
          .append("\"x\":{\"field\":{\"matcher\":{\"id\":\"byName\",\"options\":\"")
          .append(panel.xField())
          .append("\"}}},")
          .append("\"y\":{\"field\":{\"matcher\":{\"id\":\"byName\",\"options\":\"")
          .append(panel.yField())
          .append("\"}}}")
          .append("}]}");
        sb.append('}');
    }

    /**
     * Builds a Flux query for an XY chart panel. The query filters on both the
     * X and Y fields, then applies a {@code pivot()} transformation to produce
     * a single table with both field columns.
     *
     * @param panel the XY panel definition containing measurement, xField, and yField
     * @return a JSON-escaped Flux query string
     */
    private String buildXYFluxQuery(XYPanelDefinition panel) {
        return "from(bucket: \\\"madkit\\\") "
             + "|> range(start: v.timeRangeStart, stop: v.timeRangeStop) "
             + "|> filter(fn: (r) => r._measurement == \\\"" + panel.measurement() + "\\\") "
             + "|> filter(fn: (r) => r._field == \\\"" + panel.xField()
             + "\\\" or r._field == \\\"" + panel.yField() + "\\\") "
             + "|> pivot(rowKey: [\\\"_time\\\"], columnKey: [\\\"_field\\\"], valueColumn: \\\"_value\\\")";
    }

    /**
     * Converts a dashboard title to a URL-safe UID suitable for Grafana.
     *
     * @param title the dashboard title
     * @return a lowercase, hyphen-separated UID string
     */
    private String titleToUid(String title) {
        return title.toLowerCase().replaceAll("[^a-z0-9]", "-");
    }

    /**
     * Appends a JSON key-value string field to the builder.
     *
     * @param sb    the StringBuilder to append to
     * @param key   the JSON field name
     * @param value the JSON field value (not escaped — caller must pre-escape)
     */
    private void appendField(StringBuilder sb, String key, String value) {
        sb.append('"').append(key).append("\":\"").append(value).append('"');
    }
}
