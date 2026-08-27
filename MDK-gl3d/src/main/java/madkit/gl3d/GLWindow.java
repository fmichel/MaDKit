package madkit.gl3d;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetKey;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;

/** Owns a GLFW window and its thread-affine OpenGL context. */
public final class GLWindow implements AutoCloseable {
    private final String title;
    private final int requestedWidth;
    private final int requestedHeight;
    private final boolean visible;
    private final Runnable frameRenderer;
    private final CountDownLatch started = new CountDownLatch(1);
    private final AtomicReference<Throwable> startupFailure = new AtomicReference<>();
    private volatile boolean closing;
    private volatile long handle;
    private Thread renderThread;

    public GLWindow(String title, int width, int height, boolean visible, Runnable frameRenderer) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Window dimensions must be positive");
        }
        this.title = Objects.requireNonNull(title, "title");
        this.requestedWidth = width;
        this.requestedHeight = height;
        this.visible = visible;
        this.frameRenderer = Objects.requireNonNull(frameRenderer, "frameRenderer");
    }

    /** Starts the context thread and waits until GLFW/OpenGL initialization completes. */
    public synchronized void start() {
        if (renderThread != null) {
            throw new IllegalStateException("Window already started");
        }
        renderThread = new Thread(this::runLoop, "madkit-gl3d-renderer");
        renderThread.setDaemon(true);
        renderThread.start();
        try {
            started.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while starting OpenGL window", exception);
        }
        if (startupFailure.get() != null) {
            throw new IllegalStateException("Unable to initialize OpenGL window", startupFailure.get());
        }
    }

    private void runLoop() {
        try {
            if (!glfwInit()) {
                throw new IllegalStateException("GLFW initialization failed");
            }
            glfwDefaultWindowHints();
            org.lwjgl.glfw.GLFW.glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
            org.lwjgl.glfw.GLFW.glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
            org.lwjgl.glfw.GLFW.glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            org.lwjgl.glfw.GLFW.glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
            org.lwjgl.glfw.GLFW.glfwWindowHint(GLFW_VISIBLE, visible ? GLFW_TRUE : 0);
            handle = glfwCreateWindow(requestedWidth, requestedHeight, title, 0, 0);
            if (handle == 0) {
                throw new IllegalStateException("GLFW window creation failed");
            }
            glfwMakeContextCurrent(handle);
            GL.createCapabilities();
            glfwSwapInterval(1);
            glfwSetFramebufferSizeCallback(handle, (window, width, height) -> GL11C.glViewport(0, 0, width, height));
            started.countDown();
            if (visible) {
                glfwShowWindow(handle);
            }
            while (!closing && !glfwWindowShouldClose(handle)) {
                if (glfwGetKey(handle, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
                    glfwSetWindowShouldClose(handle, true);
                }
                frameRenderer.run();
                glfwSwapBuffers(handle);
                glfwPollEvents();
            }
        } catch (Throwable failure) {
            startupFailure.compareAndSet(null, failure);
            started.countDown();
        } finally {
            if (handle != 0) {
                glfwDestroyWindow(handle);
                handle = 0;
            }
            glfwTerminate();
        }
    }

    public boolean isStarted() {
        return renderThread != null && started.getCount() == 0 && startupFailure.get() == null;
    }

    public long handle() {
        return handle;
    }

    @Override
    public synchronized void close() {
        closing = true;
        if (renderThread != null && renderThread != Thread.currentThread()) {
            try {
                renderThread.join(2_000);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }
    }
}