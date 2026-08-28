# MDK-gl3d

An optional OpenGL 3.3 viewer extension for MaDKit 6. The module keeps the MaDKit core unchanged and owns its GLFW context on a dedicated render thread.

## Implemented

- `GLWindow`: GLFW initialization, OpenGL capability setup, resize handling, swap loop, and deterministic shutdown.
- `GLViewer3D`: a `madkit.simulation.Viewer` bridge whose `display()` method is thread-safe and does not access JavaFX.
- `GLTriangleDemo`: a minimal shader-based OpenGL smoke test.
- `InteropBuffer`: a tested map/unmap ownership contract for a future JCuda adapter.
- `CudaSupport`: safe, non-initializing detection of an optional JCuda driver binding.

CUDA is intentionally not a mandatory dependency: a simulation can run the OpenGL path on Linux, macOS, or Windows without NVIDIA libraries. The JCuda registration and kernel launcher are the next integration boundary and should be supplied by a platform-specific adapter.

## Build and run

From the repository root:

```bash
./gradlew :MDK-gl3d:build
./gradlew :MDK-gl3d:run
```

The demo requires a graphical session and OpenGL 3.3 support. Press **Escape** or close the window to exit. Select another LWJGL native classifier when building on another platform:

```bash
./gradlew :MDK-gl3d:run -PlwjglNatives=natives-windows
./gradlew :MDK-gl3d:run -PlwjglNatives=natives-macos
```

For applications that package CUDA separately, add JCuda and an implementation of `InteropBuffer.Mapper` at the application layer. CUDA detection can be disabled explicitly with `-Dmadkit.gl3d.cuda.disabled=true`.

## Threading contract

All OpenGL and GLFW operations occur on `madkit-gl3d-renderer`. MaDKit scheduler threads may call `display()` but must not call OpenGL methods directly. A mapper must map a buffer, launch CUDA work, and close the mapping before OpenGL draws from that buffer.

## Bees demo

The default application entry point is `madkit.gl3d.bees.GL3dBeeLauncher`. It composes a MaDKit scheduler, deterministic CPU model, immutable snapshot exchange, and the OpenGL viewer. The standalone `madkit.gl3d.demo.BeeDemo` remains available for renderer smoke testing.

The GLFW overlay displays labeled **Start**, **Pause**, **Step**, **Stop**, and **Reset** controls, lifecycle state, tick, simulated time, bee count, food, speed, measured simulation tick duration, render FPS, and smoothed render frame time in microseconds. Start, pause, step, and stop are routed to the MaDKit scheduler; reset recreates the deterministic scenario.

The 3D scene includes a resize-aware projection, a depth-tested wireframe terrain grid, a filled low-poly hive, and batched crossed-triangle flower heads. Bees use the instanced mesh path described below.

Bees now use one static low-poly mesh with a dynamic per-instance position/color buffer and an instanced draw submission. This removes the one-draw-per-bee design; the headless CPU benchmark is available below, while formal GPU/rendering measurements remain pending.

The overlay also provides **Slower** and **Faster** controls. They halve or double the configured simulation speed, clamped to the controller’s supported range.

The second control row edits the next scenario safely: **Bees-**/**Bees+** changes the worker population by 10, **Flowers-**/**Flowers+** changes flower patches by 1, and **Seed+** advances the deterministic seed. Each edit queues a reset and starts the replacement scenario stopped. Bee counts are clamped to 0–100,000 and flower counts to 0–10,000.

The **Colors** toggle switches between a single bee color and behavior-state colors: hive, searching, collecting, returning, and resting. The status overlay reports the corresponding count for each state. The **Vectors** toggle draws batched yellow last-tick velocity vectors; it is a render-only option and adds one line batch per frame. The **Trails** toggle retains up to 12 positions per active bee by stable ID and draws them in one batched line submission.

Controls currently available:

- click the labeled buttons in the overlay to control the simulation;
- `P`: pause; `S`: single-step while paused;
- `H` or `Home`: reset/overview camera;
- `F`: focus the first flower field;
- `Tab`: select the next bee; `B`: focus the selected bee; `T`: follow the selected bee;
- left-mouse drag: orbit; middle-mouse drag: pan; mouse wheel: zoom/dolly;
- `Escape` or closing the window: stop the render loop, request `SchedulingAction.SHUTDOWN`, end the viewer agent, and terminate the demo process.

For a bounded graphical smoke test on Linux, run the launcher in its own process session and send Escape through the same X display. The separate process session is important: a timeout or cleanup signal can then target the whole launcher tree rather than only the Gradle client.

```bash
Xvfb :99 -screen 0 1280x720x24 >/tmp/mdk-gl3d-xvfb.log 2>&1 &
xvfb_pid=$!
trap 'kill "$xvfb_pid" 2>/dev/null || true' EXIT
export DISPLAY=:99
setsid ./gradlew :MDK-gl3d:run --no-daemon --console=plain </dev/null >/tmp/mdk-gl3d-smoke.log 2>&1 &
launcher_pid=$!
# Wait until the window exists, then inject the real close action.
window_id=$(for i in $(seq 1 30); do xdotool search --name 'MaDKit GL3D' 2>/dev/null | head -1 && break; sleep 1; done)
xdotool key --window "$window_id" Escape
wait "$launcher_pid"
```

If the window cannot be found, do not wait indefinitely: terminate the complete process session with `kill -TERM -- -$launcher_pid`, followed by `kill -KILL -- -$launcher_pid` only as a last resort.

The overlay uses a small dependency-free bitmap font and remains render-thread-owned. Render FPS and `FRAME` are derived from a smoothed wall-clock interval between completed render-loop calls; `GPU` is an asynchronous OpenGL timer-query result. Advanced behavior toggles, GPU draw-call/allocation profiling, and physical graphical usability tests remain scheduled work.

The overlay reports both the smoothed wall-clock loop frame time (`FRAME`) and the latest completed OpenGL timer-query scene time (`GPU`). `GPU` is zero until a query result is available or when the active driver does not expose OpenGL 3.3 timer queries; it excludes buffer swap and overlay rendering.

## Headless performance benchmark

`madkit.gl3d.bees.BeePerformanceBenchmark` measures CPU simulation-tick time and immutable snapshot creation separately for 1,000, 10,000, and 100,000 bees. It does not initialize GLFW/OpenGL and does not claim GPU frame time, allocation pressure, or CUDA performance.

Run a quick smoke measurement from the repository root:

```bash
./gradlew :MDK-gl3d:beePerformanceBenchmark -PwarmupTicks=5 -PmeasuredTicks=20 --no-daemon --console=plain
```

The task prints locale-independent CSV columns for population, total/average tick time, and total/average snapshot time. Use a profiler and a graphical session before treating results as release-performance evidence; record the machine, JVM, driver, scenario, warmup, and measured tick count in `PROGRESS.md`.

## Scenarios and troubleshooting

The default compact scenario starts with a small deterministic colony suitable for learning the controls. The larger-field configuration is available through the scenario parameters and is intended for scaling experiments; population and flower changes are applied by a safe stopped reset rather than mutating a live model.

If the window does not open, verify that `DISPLAY`/Wayland access is available and that the system provides an OpenGL 3.3 driver. Headless CI should run `:MDK-gl3d:test` and `:MDK-gl3d:beePerformanceBenchmark`, not `:MDK-gl3d:run`. Missing native libraries or unsupported OpenGL versions should be treated as an initialization failure; CUDA is optional and must fall back to CPU/OpenGL.

The separation between model, controller, immutable snapshots, camera, and renderer is described in [`ARCHITECTURE.md`](ARCHITECTURE.md). The module descriptor exports the public extension packages while keeping native resource ownership inside their documented lifecycle boundaries.

The launcher forwards command-line arguments to MaDKit. Bees-specific values are supplied through MaDKit's `-D` properties: `-DbeeSeed=99`, `-DbeeCount=1000`, and `-DflowerCount=50`. Counts are clamped to 0–100,000 bees and 0–10,000 flower patches during model activation; omitted values use the compact deterministic defaults. Standard options such as `--headless`, `--start`, and explicit engine/viewer selection remain available when supported by the MaDKit kernel configuration.