package madkit.grafana.dashboard;

/**
 * Sealed base type for all Grafana panel definitions.
 * <p>
 * Permits {@link PanelDefinition} (time series, stat, and table panels) and
 * {@link XYPanelDefinition} (XY chart panels with custom X/Y field mapping).
 */
public sealed interface PanelDef permits PanelDefinition, XYPanelDefinition {

    /**
     * Returns the panel display title.
     * @return the title
     */
    String title();

    /**
     * Returns the InfluxDB measurement to query.
     * @return the measurement name
     */
    String measurement();
}
