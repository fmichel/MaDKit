package madkit.grafana.dashboard;

/**
 * Immutable definition of a Grafana XY Chart panel.
 * <p>
 * Unlike {@link PanelDefinition} (which queries a single field over time),
 * this definition maps two InfluxDB fields to the X and Y axes of
 * Grafana's built-in {@code xychart} panel (Grafana 10+).
 * This is useful whenever data should be plotted as X vs Y rather than
 * over wall-clock time — for example, iteration count vs. metric value.
 *
 * @param title       the panel display title
 * @param measurement the InfluxDB measurement to query
 * @param xField      the InfluxDB field mapped to the X axis
 * @param yField      the InfluxDB field mapped to the Y axis
 */
public record XYPanelDefinition(
    String title,
    String measurement,
    String xField,
    String yField
) implements PanelDef {}
