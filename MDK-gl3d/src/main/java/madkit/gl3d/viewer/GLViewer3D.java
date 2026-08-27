package madkit.gl3d.viewer;

import java.util.concurrent.atomic.AtomicBoolean;

import madkit.gl3d.GLWindow;
import madkit.simulation.Viewer;

/**
 * MaDKit viewer base class backed by a dedicated GLFW/OpenGL thread.
 *
 * <p>The simulation thread never touches OpenGL. Calls to {@link #display()}
 * only publish a rendering request, which keeps the OpenGL context thread-affine.</p>
 */
public abstract class GLViewer3D extends Viewer {
    private final AtomicBoolean renderRequested = new AtomicBoolean(true);
    private GLWindow window;

    protected GLViewer3D() {
        this("MaDKit GL3D", 1_280, 720, true);
    }

    protected GLViewer3D(String title, int width, int height, boolean visible) {
        window = new GLWindow(title, width, height, visible, this::renderFrame);
    }

    @Override
    protected final void onActivation() {
        super.onActivation();
        window.start();
    }

    @Override
    public final void display() {
        renderRequested.set(true);
    }

    private void renderFrame() {
        // Render continuously by default; subclasses can use this flag to skip expensive work.
        if (renderRequested.getAndSet(false) || isContinuousRendering()) {
            render();
        }
    }

    /** Returns whether frames should be rendered even without a simulation request. */
    protected boolean isContinuousRendering() {
        return true;
    }

    protected final GLWindow glWindow() {
        return window;
    }

    @Override
    protected void onEnd() {
        try {
            window.close();
        } finally {
            super.onEnd();
        }
    }
}
