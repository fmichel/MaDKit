package madkit.grafana.dashboard;

/**
 * Immutable definition of a Grafana dashboard panel.
 *
 * @param title       the panel display title
 * @param panelType   Grafana panel type ("timeseries", "stat", "table")
 * @param measurement the InfluxDB measurement to query
 * @param field       the InfluxDB field name to query
 */
public record PanelDefinition(
    String title,
    String panelType,
    String measurement,
    String field
) implements PanelDef {}
