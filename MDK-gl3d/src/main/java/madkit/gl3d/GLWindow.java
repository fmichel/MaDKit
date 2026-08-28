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
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwSetWindowTitle;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;

/** Owns a GLFW window and its thread-affine OpenGL context. */
public final class GLWindow implements AutoCloseable {
    private final String title;
    private final int requestedWidth;
    private final int requestedHeight;
    private final boolean visible;
    private final Runnable frameRenderer;
    private final Runnable cleanup;
    private GLInputHandler inputHandler;
    private final CountDownLatch started = new CountDownLatch(1);
    private final AtomicReference<Throwable> startupFailure = new AtomicReference<>();
    private volatile boolean closing;
    private volatile long handle;
    private volatile int framebufferWidth;
    private volatile int framebufferHeight;
    private Thread renderThread;
    private final AtomicReference<String> requestedTitle;

    public GLWindow(String title, int width, int height, boolean visible, Runnable frameRenderer) {
        this(title, width, height, visible, frameRenderer, new GLInputHandler() { }, () -> { });
    }

    public GLWindow(String title, int width, int height, boolean visible, Runnable frameRenderer, Runnable cleanup) {
        this(title, width, height, visible, frameRenderer, new GLInputHandler() { }, cleanup);
    }

    public GLWindow(String title, int width, int height, boolean visible, Runnable frameRenderer,
                    GLInputHandler inputHandler, Runnable cleanup) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Window dimensions must be positive");
        }
        this.title = Objects.requireNonNull(title, "title");
        this.requestedTitle = new AtomicReference<>(title);
        this.requestedWidth = width;
        this.requestedHeight = height;
        this.visible = visible;
        this.frameRenderer = Objects.requireNonNull(frameRenderer, "frameRenderer");
        this.inputHandler = Objects.requireNonNull(inputHandler, "inputHandler");
        this.cleanup = Objects.requireNonNull(cleanup, "cleanup");
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
            glfwSetFramebufferSizeCallback(handle, (window, width, height) -> {
                framebufferWidth = width;
                framebufferHeight = height;
                GL11C.glViewport(0, 0, width, height);
            });
            try (MemoryStack stack = MemoryStack.stackPush()) {
                var width = stack.mallocInt(1);
                var height = stack.mallocInt(1);
                glfwGetFramebufferSize(handle, width, height);
                framebufferWidth = width.get(0);
                framebufferHeight = height.get(0);
                GL11C.glViewport(0, 0, framebufferWidth, framebufferHeight);
            }
            glfwSetKeyCallback(handle, (window, key, scancode, action, modifiers) -> inputHandler.key(key, action, modifiers));
            glfwSetCursorPosCallback(handle, (window, x, y) -> inputHandler.cursorMoved(x, y));
            glfwSetMouseButtonCallback(handle, (window, button, action, modifiers) -> inputHandler.mouseButton(button, action, modifiers));
            glfwSetScrollCallback(handle, (window, xOffset, yOffset) -> inputHandler.scroll(xOffset, yOffset));

            started.countDown();
            if (visible) {
                glfwShowWindow(handle);
            }
            while (!closing && !glfwWindowShouldClose(handle)) {
                if (glfwGetKey(handle, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
                    glfwSetWindowShouldClose(handle, true);
                }
                String nextTitle = requestedTitle.getAndSet(null);
                if (nextTitle != null) glfwSetWindowTitle(handle, nextTitle);
                frameRenderer.run();
                glfwSwapBuffers(handle);
                glfwPollEvents();
            }
        } catch (Throwable failure) {
            startupFailure.compareAndSet(null, failure);
            started.countDown();
        } finally {
            try {
                cleanup.run();
            } finally {
                if (handle != 0) {
                    glfwDestroyWindow(handle);
                    handle = 0;
                }
                glfwTerminate();
            }
        }
    }

    public boolean isStarted() {
        return renderThread != null && started.getCount() == 0 && startupFailure.get() == null;
    }

    /** Returns true only while the native window is still alive. */
    public boolean isOpen() {
        return isStarted() && handle != 0 && !closing;
    }

    public long handle() {
        return handle;
    }

    public int framebufferWidth() {
        return framebufferWidth;
    }

    public int framebufferHeight() {
        return framebufferHeight;
    }

    /** Requests a title update; the native call is performed on the GLFW thread. */
    public void setTitle(String title) {
        requestedTitle.set(Objects.requireNonNull(title, "title"));
    }

    /** Sets a new input handler; the native call is performed on the GLFW thread. */
    public synchronized void setInputHandler(GLInputHandler inputHandler) {
        if (renderThread != null) throw new IllegalStateException("Input handler cannot change after window start");
        this.inputHandler = Objects.requireNonNull(inputHandler, "inputHandler");
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