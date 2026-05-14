# MDK-chartfx — Chart-fx Integration for MaDKit

High-performance chart-fx based viewers for MaDKit simulations.

## Overview

`MDK-chartfx` provides a thin wrapper layer over [chart-fx](https://github.com/fair-acc/chart-fx)
for embedding interactive, high-performance charts inside MaDKit simulation viewers.

The key advantage over the core `LineChartDrawer<K>` is a **non-blocking data path**:
chart-fx's `DoubleDataSet` is lock-free, so data updates from the simulation thread
do not synchronize on the JavaFX Application Thread. This eliminates the
`FXExecutor.runAndWait()` bottleneck present in the standard JavaFX chart approach.

## Wrapper Classes

| Class | Description |
|---|---|
| `ChartFxViewer` | Abstract base class for embedding any chart-fx `Chart` inside a MaDKit `Viewer`. Handles lifecycle wiring, default plugins (zoom, tooltip, axis editing), and self-rendering via chart-fx's `AnimationTimer`. |
| `XYChartViewer<K>` | Convenience subclass for XY line/scatter charts with keyed `DoubleDataSet` instances. Provides `addDataSet(K, name)` and non-blocking `addData(K, x, y)` methods with sliding-window eviction. |
| `ProbeXYChartViewer` | Probe-driven convenience viewer for monitoring organizational role populations over simulation time. Chart-fx counterpart of `RolesPopulationLineChartDrawer`. |
| `DataSetManager<K>` | Package-private helper for dataset management and eviction logic. Unit-testable without MaDKit or JavaFX. |

## Relationship to `LineChartDrawer<K>`

The wrappers are designed to feel familiar to developers who already use `LineChartDrawer<K>`:

| `LineChartDrawer<K>` | Chart-fx equivalent |
|---|---|
| `addSerie(K, name)` | `addDataSet(K, name)` |
| `addData(K, x, y)` (blocking via `runAndWait`) | `addData(K, x, y)` (**non-blocking**) |
| `getLineChartTitle()`, `getxAxisLabel()`, `getyAxisLabel()` | `getChartTitle()`, `getXAxisLabel()`, `getYAxisLabel()` |
| `RolesPopulationLineChartDrawer` | `ProbeXYChartViewer` |
| Clear-all eviction | Sliding-window eviction |

## Live Plotting & Pacing

For chart-fx demos to be visually useful, the simulation scheduler should be paced.
Use `getScheduler().setPause(50)` in your launcher's `onSimulationStart()` method
to add a 50ms delay between steps (~20 steps/second). This keeps pacing concerns
in the launcher configuration rather than in the chart code.

## Quick Start

```java
public class MyViewer extends XYChartViewer<String> {

    @Override
    protected void onActivation() {
        super.onActivation();
        addDataSet("temperature", "Temperature");
    }

    @Override
    public void display() {
        double tick = ((Number) getSimuTimer().getCurrentTime()).doubleValue();
        addData("temperature", tick, readTemperature());
        super.display();
    }

    @Override
    protected String getChartTitle() {
        return "Temperature Monitor";
    }
}
```

## Dependencies

- **MaDKit** (core)
- **chart-fx** 11.3.1 (`io.fair-acc:chartfx`)

## Build

```sh
./gradlew :MDK-chartfx:compileJava
./gradlew :MDK-chartfx:test
./gradlew :MDK-chartfx:javadoc
```
