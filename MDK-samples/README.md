# MDK-samples — MaDKit Sample Applications

Demonstration agents and simulations showcasing various features of the MaDKit framework.

## Sample Packages

| Package | Topic |
|---|---|
| `madkit.sample.launching` | Starting MaDKit, launching agents |
| `madkit.sample.agent.basic` | Minimal agent examples |
| `madkit.sample.agent.threaded` | Threaded agents with autonomous behavior |
| `madkit.sample.agent.lifecycle` | Agent lifecycle (activate, live, end) |
| `madkit.sample.agent.management` | Launching and killing agents |
| `madkit.sample.agent.daemon` | Background daemon agents |
| `madkit.sample.organization` | Groups, roles, and secured organizations |
| `madkit.sample.messaging` | Point-to-point and broadcast messaging |
| `madkit.sample.logging` | Logger configuration |
| `madkit.sample.randomization` | Randomized agent properties |
| `madkit.sample.gui.basic` | Default GUI for agents |
| `madkit.sample.gui.custom` | Custom JavaFX GUI |
| `madkit.sample.gui.properties` | Property-based agent GUI |
| `madkit.sample.chartfx` | **Chart-fx live-plotting demos** |
|---

## Chart-fx Demos (`madkit.sample.chartfx`)

Two sample simulations demonstrate chart-fx based live plotting:

### Demo A — Role Population Chart

Monitors organizational role populations over simulation time using probes.
This is the chart-fx counterpart of `RolesPopulationLineChartDrawer`.

| Class | Responsibility |
|---|---|
| `PopulationAgent` | Simulated agent that switches between "worker" and "manager" roles |
| `PopulationChartViewer` | Probe-driven chart-fx viewer (`ProbeXYChartViewer`) |
| `PopulationScheduler` | Tick-based scheduler with agent and viewer activation |
| `PopulationChartLauncher` | Simulation launcher with 50ms pacing |

**Run**: `PopulationChartLauncher.main(new String[]{"--start"})`

### Demo B — Agent Metric Chart

Plots average agent energy metrics (per-role) using `PropertyProbe` and multi-series `XYChartViewer`.

| Class | Responsibility |
|---|---|
| `MetricAgent` | Simulated agent with evolving energy metric |
| `MetricChartViewer` | Multi-series XY chart viewer (`XYChartViewer<String>`) |
| `MetricChartLauncher` | Simulation launcher with 50ms pacing |

**Run**: `MetricChartLauncher.main(new String[]{"--start"})`

### Architecture Constraints

- All demo classes are **separate top-level classes** — no inner classes for agents, viewers, or launchers
- Pacing is configured in the **launcher** (`Scheduler.setPause(50)`), not in chart code
- Data updates use the **non-blocking** chart-fx data path (no `FXExecutor.runAndWait()`)

## Build

```sh
./gradlew :MDK-samples:compileJava
```
