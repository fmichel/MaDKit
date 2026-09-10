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

## Phase F package coverage matrix

The matrix below covers every package listed above. Automated test files are listed as they exist
in `src/test/java`; `—` means that no automated test file is currently provided. GUI and chart-fx
smoke checks remain display-dependent and are not represented as automated validation.

| Package | Topic | Main sample classes | Automated test files | Manual smoke requirement |
|---|---|---|---|---|
| `madkit.sample.launching` | Starting MaDKit and launching agents | `HelloMaDKit`, `MultipleLaunching`, `CommandLineOptions` | `HelloMaDKitTest`, `HelloMaDKitRuntimeTest`, `MultipleLaunchingTest`, `CommandLineOptionsTest` | Optional: run each class's `main` method to inspect startup and command-line behavior. |
| `madkit.sample.agent.basic` | Minimal agent examples | `SimpleAgent`, `AgentWithParameters` | `SimpleAgentTest` | Optional: run each launchable class and inspect its log output. |
| `madkit.sample.agent.threaded` | Threaded agents with autonomous behavior | `ThreadedAgent`, `AutonomousAgent` | `ThreadedAgentTest` | Optional: run `ThreadedAgent`; run `AutonomousAgent` only when prepared to stop it. |
| `madkit.sample.agent.lifecycle` | Agent lifecycle (activate, live, end) | `LifecycleAgent`, `LifecycleLauncher`, `CrashInActivateDemo`, `CrashInLiveDemo` | `LifecycleAgentTest`, `LifecycleAgentRuntimeTest`, `LifecycleLauncherTest`, `CrashInActivateDemoTest`, `CrashInLiveDemoTest` | Optional: run the lifecycle and crash demonstrations and inspect the documented lifecycle logs. |
| `madkit.sample.agent.management` | Launching and killing agents | `AgentLauncher`, `AgentKiller`, `WorkerAgent` | `AgentLauncherTest` | Optional: run `AgentLauncher` and `AgentKiller`; confirm workers stop after the killer demonstration. |
| `madkit.sample.agent.daemon` | Background daemon agents | `BackgroundService`, `DaemonDemo` | `DaemonDemoTest` | Optional: run `DaemonDemo` and inspect daemon startup, heartbeat, and shutdown logs. |
| `madkit.sample.organization` | Groups, roles, and organization queries | `GroupCreator`, `RoleRequester`, `OrganizationExplorer`, `GroupAndRoleDemo` | `GroupCreatorTest`, `GroupCreatorRuntimeTest`, `RoleRequesterTest`, `OrganizationExplorerTest`, `GroupAndRoleDemoTest` | Optional: run each demo and inspect group/role creation and query results. |
| `madkit.sample.messaging` | Point-to-point and broadcast messaging | `SenderAgent`, `ReceiverAgent`, `RequestReplyDemo`, `BroadcastDemo`, `MessagingLauncher` | `SenderAgentTest`, `ReceiverAgentTest`, `RequestReplyDemoTest`, `RequestReplyDemoRuntimeTest`, `BroadcastDemoTest`, `MessagingLauncherTest` | Optional: run the demos and inspect sent, received, reply, and broadcast messages. |
| `madkit.sample.logging` | Logger configuration | `VerboseAgent`, `LogFileAgent`, `NoLogAgent` | `LogFileAgentTest` | Optional: run each class and inspect console/log-file or suppressed output as documented. |
| `madkit.sample.randomization` | Randomized agent properties | `RandomizedAgent`, `RandomizationDemo` | `RandomizedAgentTest`, `RandomizedAgentRuntimeTest`, `RandomizationDemoTest` | Optional: run `RandomizationDemo` and inspect the randomized field values. |
| `madkit.sample.gui.basic` | Default GUI for agents | `AgentWithDefaultGUI`, `ThreadedAgentWithGUI` | — | Required for GUI behavior: on a graphical desktop, launch both classes and inspect the output pane and threaded progress. |
| `madkit.sample.gui.custom` | Custom JavaFX GUI | `CustomGUIAgent`, `CustomAgentGUI` | — | Required for GUI behavior: launch `CustomGUIAgent`, inspect the panels, and click **Click me!**. |
| `madkit.sample.gui.properties` | Property-based agent GUI | `PropertyAgent` | — | Required for GUI behavior: launch `PropertyAgent`, inspect the property sheet, and edit every displayed control. |
| `madkit.sample.chartfx` | Chart-fx live plotting | `PopulationChartLauncher`, `PopulationAgent`, `PopulationScheduler`, `PopulationChartViewer`, `MetricChartLauncher`, `MetricAgent`, `MetricScheduler`, `MetricChartViewer` | — | ✅ Bounded Xvfb smoke completed for both launchers; no CI display harness or automated rendering test. |

## Chart-fx Demos (`madkit.sample.chartfx`)

Two sample simulations demonstrate chart-fx based live plotting. The viewer API is provided directly by MaDKit's `madkit.base` module in `madkit.simulation.viewer.chartfx`; no separate ChartFX integration artifact is required:

### Demo A — Role Population Chart

Monitors organizational role populations over simulation time using probes.
This is the chart-fx counterpart of `RolesPopulationLineChartDrawer`.

| Class | Responsibility |
|---|---|
| `PopulationAgent` | Simulated agent that switches between "worker" and "master" roles |
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

## Validation approach

The automated suite uses TestNG and AssertJ for headless checks. GUI construction and rendering remain display-dependent manual checks; the suite does not start a JavaFX toolkit or assert pixels, windows, or timing.

## GUI and chart-fx validation matrix

| Package | Launch class | Automated status | Validation mode | Manual smoke steps | Visible success criteria |
|---|---|---|---|---|---|
| `madkit.sample.gui.basic` | `AgentWithDefaultGUI`; `ThreadedAgentWithGUI` | No GUI-launch test | **Manual only for now** — JavaFX toolkit and display required | On a graphical desktop, launch each class with its `main` method. For `ThreadedAgentWithGUI`, leave the window open while the live steps run. | A default MaDKit window opens with an output pane; activation messages are visible. The threaded sample streams progress messages and then reports completion. |
| `madkit.sample.gui.custom` | `CustomGUIAgent` | No GUI-launch test | **Manual only for now** — custom JavaFX nodes and event handling require a display | On a graphical desktop, launch `CustomGUIAgent.main`. Inspect the center and left regions, then click **Click me!**. | The custom window opens with `Custom Center Panel`, `Left Panel`, and an agent log area. Clicking the button adds `Button clicked!` to the log. |
| `madkit.sample.gui.properties` | `PropertyAgent` | No GUI-launch test | **Manual only for now** — the meaningful property-sheet behavior is JavaFX/toolkit-dependent; structural-only tests are intentionally avoided. | On a graphical desktop, launch `PropertyAgent.main`. Inspect the property sheet and change each displayed value, including the speed slider. | The right panel groups Speed/Direction, Strength/Active, and Agent Name; each control accepts edits and the updated values can be confirmed in the GUI/property state. |
| `madkit.sample.chartfx` | `PopulationChartLauncher`; `MetricChartLauncher` | No chart-fx test | **Manual only for now** — both launchers create simulation viewers backed by JavaFX/chart-fx. | On a graphical desktop, run `PopulationChartLauncher.main(new String[]{"--start"})`; leave it running for several seconds and confirm the role chart updates. Stop it, then run `MetricChartLauncher.main(new String[]{"--start"})`; leave it running for several seconds and confirm both metric series update. | The population demo opens a chart titled `Role Populations (chart-fx)` with changing worker/master series. The metric demo opens `Agent Energy Metrics (chart-fx)` with producer/consumer average-energy series and an `Average Energy` Y-axis. The simulation remains responsive while data changes. |

The GUI and chart-fx packages remain manual/runtime-oriented rather than headless unit-test targets. The chart-fx source files were inspected and the real entry points are the two `*ChartLauncher` classes above. Both chart launchers were executed in a bounded Xvfb JavaFX session: JavaFX initialized and each simulation reached its running state. The intentional timeout exit status was `124`; it stopped the long-running demo and does not indicate a launcher failure. Source-level diagnostics for both launchers and their chart/scheduler collaborators are clean. No chart-fx TestNG test or CI display setup was added because toolkit startup, rendering pulses, probe updates, and timing are not stable headless contracts.

## Testing

Run the headless sample suite with:

```sh
./gradlew :MDK-samples:test --no-daemon --console=plain
```

## Build

```sh
./gradlew :MDK-samples:compileJava
```