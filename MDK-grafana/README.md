# MDK-grafana — MaDKit Metrics & Visualization

A MaDKit extension that provides real-time metrics collection and visualization through **Grafana** dashboards backed by **InfluxDB**. Any MaDKit agent can record data points and have them rendered as live dashboards — no additional framework required.

## Prerequisites

- **Java 25** (modular JPMS)
- **Docker** (with `docker compose` CLI plugin) — required for InfluxDB + Grafana containers
- **MaDKit 6** core library (`madkit.base` module)

## Quick Start

### 1. Add the dependency

In your project's `build.gradle`:

```groovy
dependencies {
    implementation project(':MDK-grafana')
}
```

In your `module-info.java`:

```java
requires madkit.grafana;
```

### 2. Start the infrastructure

```java
import madkit.grafana.docker.GrafanaInfrastructure;
import madkit.grafana.metrics.SimuMetrics;
import madkit.grafana.dashboard.GrafanaConnection;
import madkit.kernel.Agent;

public class MyAgent extends Agent {

    private GrafanaInfrastructure infrastructure;
    private SimuMetrics metrics;
    private GrafanaConnection connection;

    @Override
    protected void onActivation() {
        infrastructure = GrafanaInfrastructure.startDefault();
        metrics = GrafanaInfrastructure.createMetrics(infrastructure);
        connection = GrafanaInfrastructure.createDefaultConnection();
    }

    @Override
    protected void onEnd() {
        if (metrics != null) metrics.close();
    }
}
```

### 3. Record metrics

```java
metrics.record("cpu", cpuValue);
metrics.record("memory", memoryValue);
metrics.record("requests", requestCount);
```

### 4. Access the dashboard

Once started, Grafana is available at **http://localhost:3000** (default credentials: `admin` / `admin`).

## Supported Panel Types

| Panel Type      | Description                                              | Code Example                                                               |
|-----------------|----------------------------------------------------------|----------------------------------------------------------------------------|
| Time Series     | Line chart over wall-clock time                            | `dashboard.addTimeSeriesPanel("CPU", "cpu", "value");`                     |
| Stat            | Single-value display (latest / average)                      | `dashboard.addStatPanel("Current CPU", "cpu", "value");`                   |
| Table           | Raw tabular data                                         | `dashboard.addTablePanel("Raw Data", "cpu");`                              |
| XY Chart        | Scatter / line plot with custom X/Y fields (Grafana 10+)      | `dashboard.addXYChartPanel("X vs Y", "data", "xField", "yField");`        |

## Creating a Dashboard

### Option 1 — Programmatic API

Use `GrafanaDashboard` to build panels in code:

```java
var dashboard = new GrafanaDashboard("My Dashboard", connection);

dashboard.addTimeSeriesPanel("CPU Usage", "cpu", "value");
dashboard.addTimeSeriesPanel("Memory Usage", "memory", "value");
dashboard.addStatPanel("Current CPU", "cpu", "value");
dashboard.addXYChartPanel("Correlation", "data", "x", "y");

dashboard.save();
dashboard.openInBrowser();
```

### Option 2 — JSON File

For full control over panel layout, field matchers, and Grafana-specific options, define the dashboard as a **JSON resource** and post it via `GrafanaConnection.postDashboard()`.

#### Minimal JSON template

```json
{
  "title": "My Dashboard",
  "uid": "my-dashboard",
  "panels": [
    {
      "type": "timeseries",
      "title": "CPU Over Time",
      "gridPos": { "h": 8, "w": 12, "x": 0, "y": 0 },
      "targets": [
        {
          "query": "from(bucket:\"madkit\") |> range(start: -5m) |> filter(fn:(r) => r._measurement == \"cpu\")",
          "refId": "A"
        }
      ]
    }
  ],
  "refresh": "2s",
  "time": { "from": "now-5m", "to": "now" }
}
```

#### Loading and posting the JSON

```java
try (InputStream is = getClass().getResourceAsStream("/my-dashboard.json")) {
    String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
    connection.postDashboard(json);
}
```

> **💡 Tip — High refresh rate:** For near-real-time dashboards, set `"refresh": "1s"` in the JSON
> (or call `dashboard.setRefreshInterval("1s")` with the programmatic API).
> Flush intervals below 100 ms may cause excessive HTTP overhead to InfluxDB,
> and dashboard refresh below 1 second may strain the Grafana instance.

## Deleting a Dashboard

To delete a dashboard by UID from the command line:

```bash
curl -X DELETE -H "Authorization: Basic $(echo -n 'admin:admin' | base64)" \
  http://localhost:3000/api/dashboards/uid/<dashboard-uid>
```

Replace `<dashboard-uid>` with the dashboard's UID (visible in the Grafana URL).
To list all dashboards:

```bash
curl -s -u admin:admin http://localhost:3000/api/search | python3 -m json.tool
```

## Package Overview

| Package                    | Responsibility                                                                |
|----------------------------|-------------------------------------------------------------------------------|
| `madkit.grafana.metrics`   | Thread-safe metrics buffering, batching, and writing                          |
| `madkit.grafana.dashboard` | Grafana dashboard creation via HTTP API (Time Series, Stat, Table, XY Chart)  |
| `madkit.grafana.docker`    | Docker Compose lifecycle management for Grafana + InfluxDB                    |

## Architecture

```
 ┌─────────────────────┐
 │   Your Agent         │
 │  (MaDKit Agent)      │
 └───────┬─────────────┘
         │ record()
         ▼
 ┌─────────────────────┐      ┌──────────────┐
 │    SimuMetrics       │─────▶│  CsvWriter   │──▶ metrics.csv
 │  (buffer + flush)    │      └──────────────┘
 │                      │      ┌──────────────┐
 │                      │─────▶│InfluxDbWriter│──▶ InfluxDB:8086
 └─────────────────────┘      └──────────────┘
                                      │
                                      ▼
                               ┌──────────────┐
                               │   Grafana     │  ◀── http://localhost:3000
                               │  (dashboards) │
                               └──────────────┘
```

## Configuration Options

| Option           | Default              | Description                                                         |
|------------------|----------------------|---------------------------------------------------------------------|
| Grafana port     | `3000`               | Host port for Grafana UI                                            |
| InfluxDB port    | `8086`               | Host port for InfluxDB API                                          |
| InfluxDB org     | `madkit`             | InfluxDB organization name                                         |
| InfluxDB bucket  | `madkit`             | InfluxDB bucket for data                                            |
| InfluxDB token   | `madkit-dev-token`   | Authentication token (development default)                          |
| Grafana user     | `admin`              | Grafana admin username                                              |
| Grafana password | `admin`              | Grafana admin password                                              |
| Work directory   | `grafana-workdir`    | Directory where Docker Compose files are extracted                   |
| Buffer size      | `10,000`             | Max data points buffered before dropping                             |
| Flush interval   | `500 ms`             | Background flush period (configurable via constructor)               |
| Dash refresh     | `2s`                 | Grafana auto-refresh interval (configurable via `setRefreshInterval`) |

## Graceful Degradation

All Grafana integration is **fault-tolerant**. If Docker is unavailable or infrastructure fails to start, `SimuMetrics.noOp()` is used — the application runs normally without metrics collection.

## Troubleshooting

### Grafana dashboard POST fails with HTTP 403

**Symptom:** Log message `Dashboard POST failed (HTTP 403): ... Permissions needed: dashboards:create`

**Cause:** The Grafana anonymous user role is set to `Viewer` (insufficient permissions), and/or the credentials used in `GrafanaConnection` do not match the `docker-compose.yml` admin password.

**Fix:**
1. Ensure `GF_AUTH_ANONYMOUS_ORG_ROLE` is set to `Admin` in `docker-compose.yml`
2. Ensure the password passed to `GrafanaConnection` matches `GF_SECURITY_ADMIN_PASSWORD` (default: `admin`)
3. Clear Docker volumes and restart: `docker compose -f grafana-workdir/docker-compose.yml down -v`

### InfluxDB is not accessible

**Symptom:** Cannot access `http://localhost:8086`, or InfluxDB writes fail silently.

**Cause:** InfluxDB's `INIT_MODE=setup` only initializes on first run. If volumes contain stale data from a previous run with different credentials, initialization is skipped.

**Fix:**
1. Remove Docker volumes: `docker compose -f grafana-workdir/docker-compose.yml down -v`
2. Restart the stack — InfluxDB will re-initialize with the configured credentials
3. Verify access at `http://localhost:8086` (login: `madkit` / `madkit-dev`)

### Dashboard does not open in browser / panels show no data

**Symptom:** `openInBrowser()` does nothing, or the dashboard shows "No data" in all panels.

**Cause:** The dashboard UID is not captured from the Grafana API response, making `openInBrowser()` a no-op. "No data" in panels usually means InfluxDB has no data yet or the datasource provisioning failed.

**Fix:**
1. Verify InfluxDB is accessible (see above)
2. Wait a few seconds for data to be flushed and Grafana to refresh (2s auto-refresh is configured)
3. Manually navigate to `http://localhost:3000` → Dashboards → Browse to find the dashboard

### XY Chart panel shows "Missing series config" or "se[0] is undefined"

**Symptom:** The XY Chart panel renders with the error "Missing series config"
or the JavaScript error `can't access property "x", se[0] is undefined`.

**Cause:** Grafana 10.4+ `xychart` panels with `"seriesMapping":"manual"` require
each series entry to use **field matchers** (not bare field names). Each series must
include a `frame` matcher and `x`/`y` field matchers with `"id":"byName"`. Simplified
formats like `{"x":{"field":"tick"}}` or the legacy `"dims"` key are silently ignored
by Grafana, resulting in `undefined` series objects at runtime.

**Correct panel options format (Grafana 10.4+):**
```json
"options": {
  "seriesMapping": "manual",
  "series": [
    {
      "frame": { "matcher": { "id": "byIndex", "options": 0 } },
      "x": { "field": { "matcher": { "id": "byName", "options": "tick" } } },
      "y": { "field": { "matcher": { "id": "byName", "options": "value" } } }
    }
  ]
}
```

The panel also needs a `"fieldConfig":{"defaults":{},"overrides":[]}` block.

**Fix:**
1. Ensure you are using the latest `DashboardJsonBuilder` — the `addXYChartPanel()` API generates the correct matcher-based series config automatically.
2. If you manually craft panel JSON, use the full `frame` + `x`/`y` matcher structure shown above.
3. After updating the code, delete the old dashboard in Grafana and re-provision it.

### Cleaning up Docker volumes

When changing Docker Compose configuration or encountering stale state, a full cleanup is recommended:

```bash
docker compose -f grafana-workdir/docker-compose.yml down -v
```

Or programmatically: `infrastructure.stop(true)` (the `true` flag removes volumes).

## Testing

```bash
# Run unit tests (fast, no Docker required)
./gradlew :MDK-grafana:test

# Run integration tests (requires Docker)
./gradlew :MDK-grafana:integrationTest
```

## License

CeCILL-C — see the project root for full license text.