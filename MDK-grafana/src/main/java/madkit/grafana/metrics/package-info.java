/**
 * Metrics recording and writing pipeline for MaDKit simulation data.
 * <p>
 * Core classes:
 * <ul>
 *   <li>{@link madkit.grafana.metrics.SimuMetrics} — main facade for recording metrics</li>
 *   <li>{@link madkit.grafana.metrics.DataPoint} — immutable data point record</li>
 *   <li>{@link madkit.grafana.metrics.MetricsWriter} — strategy interface for metric backends</li>
 *   <li>{@link madkit.grafana.metrics.CsvWriter} — CSV file writer implementation</li>
 *   <li>{@link madkit.grafana.metrics.InfluxDbWriter} — InfluxDB writer implementation</li>
 * </ul>
 */
package madkit.grafana.metrics;
