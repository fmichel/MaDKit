package madkit.grafana;

import java.nio.file.Path;

/**
 * Immutable configuration for the Grafana + InfluxDB integration.
 * <p>
 * All fields have sensible defaults for local Docker development.
 *
 * @param grafanaUrl      Grafana base URL
 * @param grafanaUser     Grafana admin username
 * @param grafanaPassword Grafana admin password
 * @param influxUrl       InfluxDB base URL
 * @param influxOrg       InfluxDB organization
 * @param influxBucket    InfluxDB bucket
 * @param influxToken     InfluxDB authentication token
 * @param workDir         working directory for Docker Compose files
 */
public record GrafanaConfig(
    String grafanaUrl,
    String grafanaUser,
    String grafanaPassword,
    String influxUrl,
    String influxOrg,
    String influxBucket,
    String influxToken,
    Path workDir
) {
    /** Default configuration for local Docker development. */
    public static GrafanaConfig defaults() {
        return new GrafanaConfig(
            "http://localhost:3000",
            "admin",
            "admin",
            "http://localhost:8086",
            "madkit",
            "madkit",
            "madkit-dev-token",
            Path.of("grafana-workdir")
        );
    }
}
