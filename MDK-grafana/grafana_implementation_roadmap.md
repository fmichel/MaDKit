# MDK-grafana — Finalization Implementation Roadmap

> This document is the **single implementation plan** for finalizing the MDK-grafana subproject.
> It covers three work streams that can be executed sequentially:
> 1. **README.md rewrite** — clean, generic, user-facing documentation
> 2. **Example agent** — a self-contained, runnable sample inside the subproject
> 3. **Javadoc pass** — comprehensive Javadoc for every class and method

---

## Work Stream 1 — README.md Rewrite

### Goal

Rewrite `MDK-grafana/README.md` from scratch so that it is a **clean user manual** with zero references to implementation iterations, internal issue trackers, or refactoring roadmaps.

### Current Problems Identified

| # | Problem | Location |
|---|---------|----------|
| 1 | Title says "Simulation Metrics" — library is generic, not simulation-specific | Line 1 |
| 2 | Quick-start example uses `SimuLauncher` / scheduler / simulation-specific code | §2–§3 |
| 3 | "Using Simulation Time" and "Tick-Based Simulations" sections are simulation-specific | §Simulation Time, §Tick-Based |
| 4 | Architecture diagram boxes say `SimuLauncher (your application)` | §Architecture |
| 5 | "JSON-Based Dashboard Provisioning" references `bee-dashboard.json` | §JSON-Based |
| 6 | "Related Documentation" links to non-existent files (`grafana_issues.md`, `grafana_issues_report.md`, `grafana_refactoring_roadmap.md`) | §Related Documentation |
| 7 | Troubleshooting §3 links to `grafana_refactoring_roadmap.md Phase 3` | §Troubleshooting |
| 8 | Troubleshooting §1–§3 link to `grafana_issues.md §1/§2/§3` — file does not exist | §Troubleshooting |
| 9 | No documentation of the two usage approaches (programmatic vs. JSON file) as equal alternatives | Throughout |
| 10 | No example for deleting a dashboard from the command line | Missing |
| 11 | Package Overview table: second row is very long and may render poorly | §Package Overview |
| 12 | The `PanelDef` Javadoc says "tick-based simulations" — should be more generic | PanelDef.java |
| 13 | No section explaining the **types of panels** the library supports | Missing |

### New README.md Structure

The file should be rewritten with this exact structure:

```
# MDK-grafana — MaDKit Metrics & Visualization

Short intro paragraph (2-3 sentences, generic: any MaDKit agent).

## Prerequisites
  - Java 25 (JPMS)
  - Docker (with docker compose)
  - MaDKit 6

## Quick Start
### 1. Add the dependency
  build.gradle + module-info.java

### 2. Start the infrastructure
  GrafanaInfrastructure.startDefault()
  GrafanaInfrastructure.createMetrics(infrastructure)
  Brief code snippet — generic Agent, not SimuLauncher

### 3. Record metrics
  metrics.record(...) examples — generic

### 4. Access the dashboard
  http://localhost:3000

## Supported Panel Types
  Explain each panel type with a one-line description and a one-line code example:
  - **Time Series** — line chart over wall-clock time
  - **Stat** — single-value display (latest / average)
  - **Table** — raw tabular data
  - **XY Chart** — scatter / line plot with custom X/Y fields (Grafana 10+)

## Creating a Dashboard

### Option 1 — Programmatic API
  Full code example using GrafanaDashboard with addTimeSeriesPanel, addStatPanel, addXYChartPanel, save()

### Option 2 — JSON File
  Explain the approach: define dashboard as a JSON resource, post via GrafanaConnection.postDashboard()
  Show a minimal JSON template (generic — no "bees")
  Show the Java code to load and post it

## Deleting a Dashboard
  curl command-line example using Grafana API

## Package Overview
  3-row table (same packages, cleaned-up descriptions, kept concise)

## Architecture
  ASCII diagram — replace SimuLauncher with "Your Agent" or "MaDKit Agent"

## Configuration Options
  Same table (correct, keep as-is)

## Graceful Degradation
  Same paragraph (correct, keep as-is)

## Troubleshooting
  Keep the 4 existing troubleshooting entries BUT:
  - Remove ALL "See grafana_issues.md" links
  - Remove the "Ensure the fixes from grafana_refactoring_roadmap.md" sentence
  - Keep the actual fix instructions

## Testing
  Same block (correct, keep as-is)

## License
  Same line (correct, keep as-is)
```

### Specific Content to Remove

1. **Entire "Related Documentation" section** — all three referenced files are iteration artifacts that do not exist
2. All `See [grafana_issues.md]` links in troubleshooting sections (4 occurrences)
3. The sentence `Ensure the fixes from grafana_refactoring_roadmap.md Phase 3 are applied` in the "Dashboard does not open" troubleshooting entry
4. The "Using Simulation Time" subsection (simulation-specific)
5. The "Tick-Based Simulations (XY Chart)" subsection (simulation-specific; XY Chart is documented generically in "Supported Panel Types" and "Creating a Dashboard")
6. The "JSON-Based Dashboard Provisioning (Recommended)" subsection — content moves into "Creating a Dashboard > Option 2"
7. The "High Refresh Rate" subsection — move its content into a note in the "Configuration Options" section or as a tip in "Creating a Dashboard"

### Tables to Verify

All markdown tables must follow this format exactly:

```markdown
| Header 1 | Header 2 | Header 3 |
|----------|----------|----------|
| cell     | cell     | cell     |
```

Check that:
- The separator row uses at least 3 dashes per column: `|---|---|---|`
- Every row has the same number of pipe characters
- No row has trailing content after the last pipe
- Cell content is reasonably short (< 80 chars) for rendering

### Deleting a Dashboard — Exact Content

Add a new section with this curl example:

```markdown
## Deleting a Dashboard

To delete a dashboard by UID from the command line:

\```bash
curl -X DELETE -H "Authorization: Basic $(echo -n 'admin:admin' | base64)" \
  http://localhost:3000/api/dashboards/uid/<dashboard-uid>
\```

Replace `<dashboard-uid>` with the dashboard's UID (visible in the Grafana URL).
To list all dashboards:

\```bash
curl -s -u admin:admin http://localhost:3000/api/search | python3 -m json.tool
\```
```

---

## Work Stream 2 — Example Agent

### Goal

Create a self-contained, runnable example inside `MDK-grafana` that demonstrates the library using **one agent** (no simulation framework, no bees, no external project dependency).

### Package

`madkit.grafana.example`

Place source files at:
```
src/main/java/madkit/grafana/example/GrafanaExampleAgent.java
src/main/java/madkit/grafana/example/package-info.java
```

Add a JSON dashboard resource at:
```
src/main/resources/example-dashboard.json
```

### module-info.java Change

Add one export line:
```java
exports madkit.grafana.example;
```

### GrafanaExampleAgent Design

A single agent class extending `madkit.kernel.Agent` that demonstrates the full lifecycle:

```java
package madkit.grafana.example;

import madkit.kernel.Agent;
// ... other imports

/**
 * A self-contained example demonstrating the MDK-grafana library.
 * <p>
 * This agent:
 * <ol>
 *   <li>Starts the Grafana + InfluxDB Docker infrastructure in {@link #onActivation()}</li>
 *   <li>Creates a dashboard with several panel types (programmatic API)</li>
 *   <li>Records 100 data points with short pauses in {@link #onLive()} so the user
 *       can watch the dashboard update in real time</li>
 *   <li>Cleans up metrics in {@link #onEnd()}</li>
 * </ol>
 * <p>
 * Run with: {@code executeThisAgent()}
 */
public class GrafanaExampleAgent extends Agent {

    private GrafanaInfrastructure infrastructure;
    private SimuMetrics metrics;
    private GrafanaConnection connection;

    @Override
    protected void onActivation() {
        getLogger().info("Starting Grafana infrastructure...");
        infrastructure = GrafanaInfrastructure.startDefault();
        metrics = GrafanaInfrastructure.createMetrics(infrastructure);
        connection = GrafanaInfrastructure.createDefaultConnection();
        createDashboard();
        getLogger().info("Dashboard ready — open http://localhost:3000");
    }

    @Override
    protected void onLive() {
        for (int i = 0; i < 100 && isAlive(); i++) {
            double cpuValue = 30 + 40 * Math.sin(i * 0.1) + Math.random() * 10;
            double memoryValue = 50 + 20 * Math.cos(i * 0.15) + Math.random() * 5;
            double requestCount = Math.max(0, 100 + 50 * Math.sin(i * 0.2) + Math.random() * 20);

            metrics.record("cpu", cpuValue);
            metrics.record("memory", memoryValue);
            metrics.record("requests", requestCount);

            getLogger().fine(() -> String.format("Iteration %d: cpu=%.1f mem=%.1f req=%.0f",
                    i, cpuValue, memoryValue, requestCount));
            pause(500); // 500ms between data points — visible in Grafana
        }
        getLogger().info("Data generation complete.");
    }

    @Override
    protected void onEnd() {
        if (metrics != null) {
            metrics.close();
        }
        getLogger().info("Metrics closed. Dashboard remains at http://localhost:3000");
    }

    /**
     * Creates a dashboard with four panel types using the programmatic API.
     */
    private void createDashboard() {
        if (connection == null) return;
        try {
            var dashboard = new GrafanaDashboard("MDK-grafana Example", connection);
            dashboard.addTimeSeriesPanel("CPU Usage", "cpu", "value");
            dashboard.addTimeSeriesPanel("Memory Usage", "memory", "value");
            dashboard.addStatPanel("Current CPU", "cpu", "value");
            dashboard.addStatPanel("Current Memory", "memory", "value");
            dashboard.save();
            dashboard.openInBrowser();
        } catch (Exception e) {
            getLogger().warning(() -> "Dashboard creation failed: " + e.getMessage());
        }
    }

    /**
     * Entry point — launches this example agent.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        executeThisAgent();
    }
}
```

### Behavioral Requirements

| Requirement | Detail |
|-------------|--------|
| Panel types demonstrated | Time Series (2 panels), Stat (2 panels) |
| Data shape | Sinusoidal + noise — visually interesting in Grafana |
| Iteration count | 100 |
| Pause between iterations | 500 ms (total run ≈ 50 seconds) |
| No simulation dependency | Uses `Agent`, not `SimuAgent` or `SimuLauncher` |
| Metrics cleanup | `metrics.close()` in `onEnd()` |
| Infrastructure | Uses `GrafanaInfrastructure.startDefault()` (starts Docker if needed) |
| Dashboard creation | Programmatic API (`GrafanaDashboard`) |
| Entry point | `main(String[])` calling `executeThisAgent()` |

### example-dashboard.json

Also provide a static JSON dashboard file that can be used to demonstrate the "Option 2 — JSON File" approach from the README. This file is a resource that users can reference:

```json
{
  "uid": "mdk-grafana-example",
  "title": "MDK-grafana Example",
  "panels": [
    {
      "id": 1,
      "title": "CPU Usage",
      "type": "timeseries",
      "gridPos": { "h": 8, "w": 12, "x": 0, "y": 0 },
      "targets": [{
        "refId": "A",
        "datasource": { "type": "influxdb", "uid": "influxdb-madkit" },
        "query": "from(bucket: \"madkit\") |> range(start: v.timeRangeStart, stop: v.timeRangeStop) |> filter(fn: (r) => r._measurement == \"cpu\") |> filter(fn: (r) => r._field == \"value\")"
      }]
    },
    {
      "id": 2,
      "title": "Memory Usage",
      "type": "timeseries",
      "gridPos": { "h": 8, "w": 12, "x": 12, "y": 0 },
      "targets": [{
        "refId": "A",
        "datasource": { "type": "influxdb", "uid": "influxdb-madkit" },
        "query": "from(bucket: \"madkit\") |> range(start: v.timeRangeStart, stop: v.timeRangeStop) |> filter(fn: (r) => r._measurement == \"memory\") |> filter(fn: (r) => r._field == \"value\")"
      }]
    },
    {
      "id": 3,
      "title": "Current CPU",
      "type": "stat",
      "gridPos": { "h": 8, "w": 12, "x": 0, "y": 8 },
      "targets": [{
        "refId": "A",
        "datasource": { "type": "influxdb", "uid": "influxdb-madkit" },
        "query": "from(bucket: \"madkit\") |> range(start: v.timeRangeStart, stop: v.timeRangeStop) |> filter(fn: (r) => r._measurement == \"cpu\") |> filter(fn: (r) => r._field == \"value\")"
      }]
    },
    {
      "id": 4,
      "title": "Current Memory",
      "type": "stat",
      "gridPos": { "h": 8, "w": 12, "x": 12, "y": 8 },
      "targets": [{
        "refId": "A",
        "datasource": { "type": "influxdb", "uid": "influxdb-madkit" },
        "query": "from(bucket: \"madkit\") |> range(start: v.timeRangeStart, stop: v.timeRangeStop) |> filter(fn: (r) => r._measurement == \"memory\") |> filter(fn: (r) => r._field == \"value\")"
      }]
    }
  ],
  "refresh": "2s",
  "time": { "from": "now-5m", "to": "now" },
  "timezone": "browser",
  "schemaVersion": 39
}
```

### package-info.java

```java
/**
 * A self-contained example demonstrating the MDK-grafana library.
 * <p>
 * Run {@link madkit.grafana.example.GrafanaExampleAgent} to see
 * real-time metrics flowing into a Grafana dashboard.
 *
 * @see madkit.grafana.example.GrafanaExampleAgent
 */
package madkit.grafana.example;
```

---

## Work Stream 3 — Javadoc Pass

### Goal

Every class, interface, record, method (including `private`), field, and constructor in the `src/main/java` tree must have a Javadoc comment. Proofread existing Javadoc for accuracy and completeness.

### Audit of Current State

Below is a per-file audit. ✅ = has Javadoc, ❌ = missing, ⚠️ = needs correction.

#### `madkit.grafana` package

| File | Class-level | Methods/Fields |
|------|-------------|----------------|
| `GrafanaConfig.java` | ✅ | ✅ (record — auto-documented via `@param`) |
| `package-info.java` | ✅ | N/A |

#### `madkit.grafana.dashboard` package

| File | Class-level | Methods/Fields |
|------|-------------|----------------|
| `PanelDef.java` | ⚠️ "tick-based simulations" phrasing — should be generic | ✅ (`title()`, `measurement()`) |
| `PanelDefinition.java` | ✅ | ✅ (record) |
| `XYPanelDefinition.java` | ⚠️ "tick-based simulations" phrasing | ✅ (record) |
| `DashboardJsonBuilder.java` | ✅ | ❌ `appendPanel`, `buildFluxQuery`, `appendXYPanel`, `buildXYFluxQuery`, `titleToUid` (marked private but still need Javadoc per requirements), `appendField` |
| `GrafanaConnection.java` | ✅ | ❌ `extractUrlFromResponse` (package-private, missing purpose doc) |
| `GrafanaDashboard.java` | ✅ | ✅ (all public methods) but ❌ `dashboardUrl` field, ❌ `refreshInterval` field, ❌ `panels` field, ❌ `title` field, ❌ `connection` field, ❌ `jsonBuilder` field |
| `package-info.java` | ✅ | N/A |

#### `madkit.grafana.docker` package

| File | Class-level | Methods/Fields |
|------|-------------|----------------|
| `DockerComposeRunner.java` | ✅ | ❌ `buildCommand` (private) |
| `GrafanaInfrastructure.java` | ✅ | ❌ private methods: `verifyDockerAvailable`, `extractBundledResources`, `startDockerStack`, `awaitInfrastructureReady`, `extractResource`, `composeFilePath`, `grafanaHealthUrl`, `influxDbHealthUrl`. ❌ fields: `workDir`, `grafanaPort`, `influxDbPort`, `dockerRunner`, `healthChecker`, `BUNDLED_RESOURCES`, `DEFAULT_TIMEOUT` |
| `HealthChecker.java` | ✅ | ❌ field `httpClient`, ❌ constant `POLL_INTERVAL_MS` |
| `package-info.java` | ✅ | N/A |

#### `madkit.grafana.metrics` package

| File | Class-level | Methods/Fields |
|------|-------------|----------------|
| `MetricsWriter.java` | ✅ | ✅ |
| `DataPoint.java` | ✅ | ✅ |
| `SimuMetrics.java` | ✅ | ❌ private methods: `flushBuffer`, `drainRemainingPoints`, `delegateToWriters`, `closeAllWriters`, `createFlushExecutor`, `epochNanos`. ❌ fields: `buffer`, `writers`, `flushExecutor`, `closed`. ❌ constants: `DEFAULT_BUFFER_SIZE`, `DEFAULT_FLUSH_INTERVAL_MS`, `BATCH_THRESHOLD` (these are package-private, used in tests) |
| `NoOpSimuMetrics.java` | ✅ | ❌ constant `INSTANCE`, ❌ constructor |
| `CsvWriter.java` | ✅ | ❌ private methods: `formatTags`, `formatExtraFields`. ❌ fields: `writer`. ❌ constant `HEADER` |
| `InfluxDbWriter.java` | ✅ | ❌ fields: `writeUrl`, `token`, `httpClient` |
| `package-info.java` | ✅ | N/A |

### Javadoc Changes Required

Below is the exhaustive list of Javadoc to add or fix. Each entry specifies the file, the target, and the content.

#### `PanelDef.java` — Fix class-level Javadoc

**Current:**
```java
/**
 * Sealed base type for Grafana panel definitions.
 * <p>
 * Permits {@link PanelDefinition} (time-based panels) and
 * {@link XYPanelDefinition} (XY chart panels for tick-based simulations).
 */
```

**Replace with:**
```java
/**
 * Sealed base type for all Grafana panel definitions.
 * <p>
 * Permits {@link PanelDefinition} (time series, stat, and table panels) and
 * {@link XYPanelDefinition} (XY chart panels with custom X/Y field mapping).
 */
```

#### `XYPanelDefinition.java` — Fix class-level Javadoc

**Current first paragraph says** "tick-based simulations".

**Replace with:**
```java
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
```

#### `DashboardJsonBuilder.java` — Add Javadoc to all private methods

Add Javadoc to these 6 methods:

```java
/**
 * Appends a standard panel (time series, stat, or table) to the JSON output.
 *
 * @param sb    the StringBuilder to append to
 * @param panel the panel definition
 * @param index zero-based panel index (used for id and grid position)
 */
private void appendPanel(...)

/**
 * Builds a Flux query for a standard panel that filters on a single field.
 *
 * @param panel the panel definition containing measurement and field
 * @return a JSON-escaped Flux query string
 */
private String buildFluxQuery(...)

/**
 * Appends an XY chart panel to the JSON output, including the
 * {@code fieldConfig}, {@code options} with field matchers, and the pivot query.
 *
 * @param sb    the StringBuilder to append to
 * @param panel the XY panel definition
 * @param index zero-based panel index (used for id and grid position)
 */
private void appendXYPanel(...)

/**
 * Builds a Flux query for an XY chart panel. The query filters on both the
 * X and Y fields, then applies a {@code pivot()} transformation to produce
 * a single table with both field columns.
 *
 * @param panel the XY panel definition containing measurement, xField, and yField
 * @return a JSON-escaped Flux query string
 */
private String buildXYFluxQuery(...)

/**
 * Converts a dashboard title to a URL-safe UID suitable for Grafana.
 * Replaces all non-alphanumeric characters with hyphens and lowercases the result.
 *
 * @param title the dashboard title
 * @return a lowercase, hyphen-separated UID string
 */
private String titleToUid(...) // already has Javadoc — keep as-is

/**
 * Appends a JSON key-value string field to the builder.
 *
 * @param sb    the StringBuilder to append to
 * @param key   the JSON field name
 * @param value the JSON field value (not escaped — caller must pre-escape)
 */
private void appendField(...)
```

#### `GrafanaDashboard.java` — Add field Javadoc

```java
/** Logger for dashboard operations. */
private static final Logger LOGGER = ...

/** The dashboard display title. */
private final String title;

/** The Grafana HTTP API connection. */
private final GrafanaConnection connection;

/** The JSON model builder. */
private final DashboardJsonBuilder jsonBuilder;

/** Ordered list of panels added to this dashboard. */
private final List<PanelDef> panels = ...

/** The dashboard URL path returned by Grafana after saving, or {@code null}. */
private String dashboardUrl;

/** The auto-refresh interval in Grafana duration format (e.g., "2s"). */
private String refreshInterval = "2s";
```

#### `DockerComposeRunner.java` — Add private method Javadoc

```java
/**
 * Builds the full command list for a docker compose invocation.
 *
 * @param composeFile the path to the docker-compose.yml file
 * @param args        additional docker compose arguments
 * @return the complete command list
 */
private List<String> buildCommand(...)
```

#### `GrafanaInfrastructure.java` — Add Javadoc to all private methods and fields

Fields:
```java
/** Working directory where Docker Compose files are extracted. */
private final Path workDir;

/** Host port for the Grafana UI container. */
private final int grafanaPort;

/** Host port for the InfluxDB API container. */
private final int influxDbPort;

/** Delegate for executing docker compose commands. */
private final DockerComposeRunner dockerRunner;

/** Delegate for polling HTTP health endpoints. */
private final HealthChecker healthChecker;
```

Constants:
```java
/** Maximum time to wait for each service to become healthy. */
private static final Duration DEFAULT_TIMEOUT = ...

/** Classpath resources bundled in the JAR that must be extracted before starting Docker. */
private static final String[] BUNDLED_RESOURCES = ...
```

Private methods — add Javadoc for each of:
- `verifyDockerAvailable()` — Throws if Docker CLI is not on the PATH.
- `extractBundledResources()` — Creates the work directory and copies all bundled resources.
- `startDockerStack()` — Runs `docker compose up -d`.
- `awaitInfrastructureReady()` — Polls both InfluxDB and Grafana health endpoints.
- `extractResource(String)` — Copies a single classpath resource to the work directory.
- `composeFilePath()` — Returns the resolved path to `docker-compose.yml`.
- `grafanaHealthUrl()` — Returns the Grafana health endpoint URL.
- `influxDbHealthUrl()` — Returns the InfluxDB health endpoint URL.

#### `HealthChecker.java` — Add field Javadoc

```java
/** The HTTP client used for health-check requests. */
private final HttpClient httpClient;

/** Interval in milliseconds between successive health-check polls. */
private static final long POLL_INTERVAL_MS = 1_000;
```

#### `SimuMetrics.java` — Add Javadoc to fields, constants, and private methods

Constants (package-private, used in tests):
```java
/** Default capacity of the data-point buffer. */
static final int DEFAULT_BUFFER_SIZE = 10_000;

/** Default interval in milliseconds between background flushes. */
static final long DEFAULT_FLUSH_INTERVAL_MS = 500;

/** Maximum number of data points drained per flush cycle. */
static final int BATCH_THRESHOLD = 100;
```

Fields:
```java
/** Bounded buffer holding data points between record() calls and flush cycles. */
private final BlockingQueue<DataPoint> buffer;

/** Writers that receive flushed batches (InfluxDB, CSV, etc.). */
private final List<MetricsWriter> writers;

/** Single-thread scheduled executor that triggers periodic flushes. */
private final ScheduledExecutorService flushExecutor;

/** Guard flag: once true, all record() calls are silently discarded. */
private volatile boolean closed = false;
```

Private methods:
- `flushBuffer()` — Drains up to BATCH_THRESHOLD points from the buffer and delegates to writers.
- `drainRemainingPoints()` — Called on close: drains all remaining points and flushes.
- `delegateToWriters(List)` — Sends a batch to every registered writer.
- `closeAllWriters()` — Closes every registered writer, logging errors.
- `createFlushExecutor()` — Creates a daemon ScheduledExecutorService for background flushing.
- `epochNanos()` — Returns the current wall-clock time in nanoseconds.

#### `NoOpSimuMetrics.java` — Add Javadoc

```java
/** Singleton instance returned by {@link SimuMetrics#noOp()}. */
static final NoOpSimuMetrics INSTANCE = ...

/**
 * Private constructor — prevents instantiation outside this class.
 * Does not call super() with writers to avoid buffer and flush thread creation.
 */
private NoOpSimuMetrics() { ... }
```

#### `CsvWriter.java` — Add Javadoc

Fields / constants:
```java
/** The CSV column header written as the first line of new files. */
private static final String HEADER = ...

/** The underlying writer for appending CSV lines. */
private final PrintWriter writer;
```

Private methods:
```java
/**
 * Formats a tag map as a semicolon-separated {@code key=value} string.
 *
 * @param tags the tag map (may be null or empty)
 * @return the formatted string, or empty string if no tags
 */
private String formatTags(...)

/**
 * Formats an extra-fields map as a semicolon-separated {@code key=value} string.
 *
 * @param extraFields the extra-fields map (may be null or empty)
 * @return the formatted string, or empty string if no extra fields
 */
private String formatExtraFields(...)
```

#### `InfluxDbWriter.java` — Add field Javadoc

```java
/** The full InfluxDB v2 write API URL including org, bucket, and precision parameters. */
private final String writeUrl;

/** The InfluxDB authentication token. */
private final String token;

/** The HTTP client used for write requests. */
private final HttpClient httpClient;
```

---

## Execution Order and Dependencies

```
Work Stream 1 (README)     — no code dependency, can be done first
Work Stream 2 (Example)    — depends on module-info.java update
Work Stream 3 (Javadoc)    — no dependency, can be done in parallel
```

**Recommended execution order:** 2 → 1 → 3

- Start with the Example (Stream 2) because the README references it
- Then rewrite the README (Stream 1) using the example in code snippets
- Finally do the Javadoc pass (Stream 3) as a polish step

## Files Modified (Summary)

| Work Stream | Files Modified | Files Created |
|-------------|----------------|---------------|
| 1 (README) | `README.md` | — |
| 2 (Example) | `module-info.java` | `GrafanaExampleAgent.java`, `package-info.java`, `example-dashboard.json` |
| 3 (Javadoc) | `PanelDef.java`, `XYPanelDefinition.java`, `DashboardJsonBuilder.java`, `GrafanaDashboard.java`, `DockerComposeRunner.java`, `GrafanaInfrastructure.java`, `HealthChecker.java`, `SimuMetrics.java`, `NoOpSimuMetrics.java`, `CsvWriter.java`, `InfluxDbWriter.java` | — |

## Validation Criteria

| Criterion | How to Verify |
|-----------|---------------|
| README has no iteration artifacts | `grep -i "grafana_issues\|grafana_refactoring\|PROGRESS\|grafana_issues_report" README.md` returns nothing |
| README has no broken links | No `[...](...)` links to non-existent `.md` files |
| README tables render correctly | Preview in any Markdown renderer |
| README has a "Deleting a Dashboard" section | Section exists with `curl -X DELETE` example |
| README explains both dashboard approaches | "Option 1 — Programmatic API" and "Option 2 — JSON File" sections exist |
| README is not simulation-specific | No occurrence of `SimuLauncher`, `Scheduler`, `Viewer`, `SimuAgent`, `TickBasedTimer`, `bees`, `bee` |
| Example agent compiles | `./gradlew :MDK-grafana:compileJava` succeeds |
| Example agent runs | `./gradlew :MDK-grafana:run` or executing `GrafanaExampleAgent.main()` |
| Example dashboard JSON is valid | `python3 -m json.tool < src/main/resources/example-dashboard.json` succeeds |
| All Javadoc present | `./gradlew :MDK-grafana:javadoc` produces zero warnings for missing Javadoc |
| All tests pass | `./gradlew :MDK-grafana:test` succeeds |
