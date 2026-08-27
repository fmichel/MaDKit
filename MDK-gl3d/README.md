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
