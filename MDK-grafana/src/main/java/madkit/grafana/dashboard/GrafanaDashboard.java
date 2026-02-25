package madkit.grafana.dashboard;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Creates and manages Grafana dashboards via the Grafana HTTP API.
 * <p>
 * JSON model construction is delegated to {@link DashboardJsonBuilder}.
 */
public class GrafanaDashboard {

    /** Logger for dashboard operations. */
    private static final Logger LOGGER = Logger.getLogger(GrafanaDashboard.class.getName());

    /** The dashboard display title. */
    private final String title;
    /** The Grafana HTTP API connection. */
    private final GrafanaConnection connection;
    /** The JSON model builder. */
    private final DashboardJsonBuilder jsonBuilder;
    /** Ordered list of panels added to this dashboard. */
    private final List<PanelDef> panels = new ArrayList<>();
    /** The dashboard URL path returned by Grafana after saving, or {@code null}. */
    private String dashboardUrl;
    /** The auto-refresh interval in Grafana duration format (e.g., "2s"). */
    private String refreshInterval = "2s";

    /**
     * Creates a new dashboard manager.
     *
     * @param title      the dashboard title displayed in Grafana
     * @param connection the Grafana HTTP API connection
     */
    public GrafanaDashboard(String title, GrafanaConnection connection) {
        this(title, connection, new DashboardJsonBuilder());
    }

    /**
     * Constructor with injectable JSON builder for testing.
     *
     * @param title       the dashboard title
     * @param connection  the Grafana HTTP API connection
     * @param jsonBuilder the JSON builder to use
     */
    GrafanaDashboard(String title, GrafanaConnection connection, DashboardJsonBuilder jsonBuilder) {
        this.title = title;
        this.connection = connection;
        this.jsonBuilder = jsonBuilder;
    }

    /**
     * Sets the dashboard auto-refresh interval.
     * <p>
     * Common values: {@code "1s"}, {@code "2s"}, {@code "5s"}, {@code "10s"}, {@code "30s"}, {@code "1m"}.
     *
     * @param interval the refresh interval in Grafana duration format
     */
    public void setRefreshInterval(String interval) {
        this.refreshInterval = interval;
    }

    /**
     * Adds a time series panel to the dashboard.
     *
     * @param panelTitle  the panel display title
     * @param measurement the InfluxDB measurement name
     * @param field       the InfluxDB field name
     * @return the created panel definition
     */
    public PanelDefinition addTimeSeriesPanel(String panelTitle, String measurement, String field) {
        var panel = new PanelDefinition(panelTitle, "timeseries", measurement, field);
        panels.add(panel);
        return panel;
    }

    /**
     * Adds a stat panel to the dashboard.
     *
     * @param panelTitle  the panel display title
     * @param measurement the InfluxDB measurement name
     * @param field       the InfluxDB field name
     * @return the created panel definition
     */
    public PanelDefinition addStatPanel(String panelTitle, String measurement, String field) {
        var panel = new PanelDefinition(panelTitle, "stat", measurement, field);
        panels.add(panel);
        return panel;
    }

    /**
     * Adds a table panel to the dashboard.
     *
     * @param panelTitle  the panel display title
     * @param measurement the InfluxDB measurement name
     * @return the created panel definition
     */
    public PanelDefinition addTablePanel(String panelTitle, String measurement) {
        var panel = new PanelDefinition(panelTitle, "table", measurement, "value");
        panels.add(panel);
        return panel;
    }

    /**
     * Adds an XY chart panel to the dashboard.
     * <p>
     * XY chart panels plot one InfluxDB field against another, making them
     * ideal for tick-based simulations where the X axis represents the
     * simulation tick rather than wall-clock time.
     * <p>
     * Requires <b>Grafana 10+</b> (the "xychart" panel type is built-in).
     *
     * @param panelTitle  the panel display title
     * @param measurement the InfluxDB measurement name
     * @param xField      the field for the X axis (e.g., "tick")
     * @param yField      the field for the Y axis (e.g., "value")
     * @return the created XY panel definition
     */
    public XYPanelDefinition addXYChartPanel(String panelTitle, String measurement,
                                              String xField, String yField) {
        var panel = new XYPanelDefinition(panelTitle, measurement, xField, yField);
        panels.add(panel);
        return panel;
    }

    /**
     * Saves (creates or updates) the dashboard via the Grafana HTTP API.
     *
     * @throws IOException          if the HTTP request fails
     * @throws InterruptedException if the current thread is interrupted
     */
    public void save() throws IOException, InterruptedException {
        String json = jsonBuilder.build(title, panels, refreshInterval);
        String url = connection.postDashboard(json);
        if (url != null) {
            this.dashboardUrl = url;
        }
    }

    /**
     * Exports the dashboard model as a Grafana-compatible JSON string.
     *
     * @return the dashboard JSON string
     */
    public String exportJson() {
        return jsonBuilder.build(title, panels, refreshInterval);
    }

    /**
     * Loads a dashboard from a previously exported JSON file.
     *
     * @param jsonFile   the path to the JSON file
     * @param connection the Grafana HTTP API connection
     * @return the imported dashboard
     * @throws IOException          if the file cannot be read or the HTTP request fails
     * @throws InterruptedException if the current thread is interrupted
     */
    public static GrafanaDashboard fromJson(Path jsonFile, GrafanaConnection connection)
            throws IOException, InterruptedException {
        String json = Files.readString(jsonFile);
        var dashboard = new GrafanaDashboard("imported", connection);
        connection.postDashboard(json);
        return dashboard;
    }

    /**
     * Opens the dashboard in the system's default web browser.
     *
     * @throws IOException if the browser cannot be launched
     */
    public void openInBrowser() throws IOException {
        if (dashboardUrl != null && Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(URI.create(connection.getBaseUrl() + dashboardUrl));
        }
    }

    /**
     * Returns a defensive copy of the current panel list.
     *
     * @return an unmodifiable list of panel definitions
     */
    List<PanelDef> getPanels() {
        return List.copyOf(panels);
    }
}
