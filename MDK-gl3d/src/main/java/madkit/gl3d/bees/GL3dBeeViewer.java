package madkit.gl3d.bees;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_B;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_H;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_HOME;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_P;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_TAB;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_T;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE;

import java.util.concurrent.atomic.AtomicBoolean;

import madkit.action.SchedulingAction;
import madkit.gl3d.GLInputHandler;
import madkit.gl3d.gui.ControlOverlay;
import madkit.gl3d.gui.SimulationControlPanel;
import madkit.gl3d.gui.SimulationStatus;
import madkit.gl3d.viewer.BeeSceneRenderer;
import madkit.gl3d.viewer.GLViewer3D;
import madkit.messages.SchedulingMessage;
import static madkit.simulation.SimuOrganization.ENGINE_GROUP;
import static madkit.simulation.SimuOrganization.SCHEDULER_ROLE;

/** MaDKit viewer that consumes immutable GL3D bees snapshots. */
public final class GL3dBeeViewer extends GLViewer3D {
    private final BeeSceneRenderer renderer = new BeeSceneRenderer();
    private SimulationControlPanel controls;
    private ControlOverlay overlay;
    private double cursorX;
    private double cursorY;
    private double previousX;
    private double previousY;
    private boolean orbiting;
    private boolean panning;
    private boolean movedSincePress;
    private int selectedBeeIndex = -1;
    private boolean followSelected;
    private long previousRenderNanos;
    private double renderFrameMicros;
    private double renderFps;
    private final AtomicBoolean shutdownRequested = new AtomicBoolean();

    public GL3dBeeViewer() {
        super("MaDKit GL3D — Bees", 1_280, 720, true);
    }

    @Override
    public void render() {
        GL3dBeeModel model = getModel();
        if (controls == null) {
            controls = new SimulationControlPanel(model.controller(), this::schedule);
            overlay = new ControlOverlay(controls);
        }
        int width = glWindow().framebufferWidth();
        int height = glWindow().framebufferHeight();
        long nowNanos = System.nanoTime();
        if (previousRenderNanos != 0 && nowNanos > previousRenderNanos) {
            double frameMicros = (nowNanos - previousRenderNanos) / 1_000.0;
            renderFrameMicros = renderFrameMicros == 0 ? frameMicros
                    : renderFrameMicros * 0.9 + frameMicros * 0.1;
            renderFps = 1_000_000.0 / renderFrameMicros;
        }
        previousRenderNanos = nowNanos;
        
        float aspectRatio = height > 0 ? (float) width / height : 16f / 9f;
        renderer.render(model.latestSnapshot(), aspectRatio, controls.stateColors(), controls.debugVectors(),
                controls.trails());
        if (followSelected) focusSelected(model.latestSnapshot());
        SimulationStatus status = controls.status(renderFps, renderFrameMicros, renderer.gpuFrameMicros());
        overlay.render(width, height, status,
                "P PAUSE  S STEP  H/HOME RESET  F FLOWERS  B BEE  TAB SELECT  T FOLLOW  ESC EXIT");
        glWindow().setTitle("MaDKit GL3D Bees | " + status.state() + " | tick " + status.tick()
                + " | bees " + status.beeCount());
    }

    @Override
    protected void onGlCleanup() {
        try {
            if (overlay != null) overlay.close();
            renderer.close();
        } finally {
            requestApplicationShutdown();
        }
    }

    /**
     * Propagates a native-window close to the MaDKit lifecycle. GLFW owns the render thread,
     * so merely leaving its loop is insufficient: the viewer agent and scheduler would
     * otherwise remain alive and keep a launcher process running.
     */
    private void requestApplicationShutdown() {
        if (!shutdownRequested.compareAndSet(false, true)) return;
        try {
            send(new SchedulingMessage(SchedulingAction.SHUTDOWN), getCommunity(), ENGINE_GROUP, SCHEDULER_ROLE);
        } finally {
            // This is invoked by the render thread; onEnd() calls window.close(), which is
            // intentionally non-blocking when close is requested from that same thread.
            killAgent(this, 1);
        }
    }

    {
        setInputHandler(new GLInputHandler() {
            @Override
            public void key(int key, int action, int modifiers) {
                if (action != GLFW_PRESS) return;
                if (key == GLFW_KEY_P) clickAction("pause");
                if (key == GLFW_KEY_S) clickAction("step");
                if (key == GLFW_KEY_H || key == GLFW_KEY_HOME) renderer.camera().reset();
                if (key == GLFW_KEY_F) {
                    GL3dBeeModel beeModel = getModel();
                    if (!beeModel.latestSnapshot().flowers().isEmpty()) {
                        var flower = beeModel.latestSnapshot().flowers().get(0).position();
                        renderer.camera().focus(new org.joml.Vector3f((float) flower.x(), (float) flower.y(), (float) flower.z()));
                    }
                }
                if (key == GLFW_KEY_ESCAPE) glWindow().close();
                if (key == GLFW_KEY_B) focusSelected(beeModel().latestSnapshot());
                if (key == GLFW_KEY_TAB) selectNextBee(beeModel().latestSnapshot());
                if (key == GLFW_KEY_T) followSelected = !followSelected;
            }

            @Override
            public void cursorMoved(double x, double y) {
                if (orbiting) renderer.camera().orbit((float) (x - previousX) * 0.01f,
                        (float) (y - previousY) * 0.01f);
                if (panning) renderer.camera().pan((float) (x - previousX), (float) (y - previousY));
                if (orbiting || panning) movedSincePress = true;
                previousX = x;
                previousY = y;
                cursorX = x;
                cursorY = y;
            }

            @Override
            public void mouseButton(int button, int action, int modifiers) {
                if (button == GLFW_MOUSE_BUTTON_LEFT) {
                    if (action == GLFW_PRESS) {
                        orbiting = true;
                        movedSincePress = false;
                    } else if (action == GLFW_RELEASE) {
                        orbiting = false;
                        if (!movedSincePress && controls != null) controls.click(cursorX, cursorY);
                    }
                }
                if (button == GLFW_MOUSE_BUTTON_MIDDLE) {
                    panning = action != GLFW_RELEASE;
                    if (action == GLFW_PRESS) movedSincePress = false;
                }
            }

            @Override
            public void scroll(double xOffset, double yOffset) {
                renderer.camera().zoom((float) -yOffset);
            }
        });
    }

    private void clickAction(String action) {
        if (controls != null) controls.click(actionX(action), 20);
    }

    private void schedule(String action) {
        SchedulingAction schedulingAction = switch (action) {
            case "start" -> SchedulingAction.RUN;
            case "pause" -> SchedulingAction.PAUSE;
            case "step" -> SchedulingAction.STEP;
            case "stop" -> SchedulingAction.SHUTDOWN;
            default -> null;
        };
        if (schedulingAction != null) send(new SchedulingMessage(schedulingAction), getCommunity(), ENGINE_GROUP, SCHEDULER_ROLE);
    }

    private static double actionX(String action) {
        return switch (action) {
            case "start" -> 20;
            case "pause" -> 115;
            case "step" -> 210;
            case "stop" -> 305;
            default -> -1;
        };
    }

    private GL3dBeeModel beeModel() {
        return getModel();
    }

    private void selectNextBee(ColonySnapshot snapshot) {
        if (snapshot.bees().isEmpty()) {
            selectedBeeIndex = -1;
            followSelected = false;
            return;
        }
        selectedBeeIndex = (selectedBeeIndex + 1) % snapshot.bees().size();
    }

    private void focusSelected(ColonySnapshot snapshot) {
        if (snapshot.bees().isEmpty()) return;
        if (selectedBeeIndex < 0 || selectedBeeIndex >= snapshot.bees().size()) selectedBeeIndex = 0;
        var position = snapshot.bees().get(selectedBeeIndex).position();
        renderer.camera().focus(new org.joml.Vector3f((float) position.x(), (float) position.y(), (float) position.z()));
    }
}
