package madkit.gl3d.demo;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_H;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_HOME;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_P;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE;

import madkit.gl3d.GLInputHandler;
import madkit.gl3d.GLWindow;
import madkit.gl3d.bees.BeeColonyModel;
import madkit.gl3d.bees.ColonySnapshotExchange;
import madkit.gl3d.bees.SimulationController;
import madkit.gl3d.gui.ControlOverlay;
import madkit.gl3d.gui.SimulationControlPanel;
import madkit.gl3d.viewer.BeeSceneRenderer;

/** Runnable CPU/OpenGL 3D bees smoke demonstration. */
public final class BeeDemo {
    private BeeDemo() {
    }

    public static void main(String[] args) throws InterruptedException {
        BeeColonyModel model = new BeeColonyModel(42, 250, 12);
        SimulationController controller = new SimulationController(model);
        controller.start();
        ColonySnapshotExchange snapshots = new ColonySnapshotExchange();
        snapshots.publish(model.colonySnapshot());
        BeeSceneRenderer renderer = new BeeSceneRenderer();
        SimulationControlPanel controls = new SimulationControlPanel(controller, ignored -> { });
        ControlOverlay overlay = new ControlOverlay(controls);
        double[] cursor = new double[2];
        double[] previousCursor = new double[2];
        boolean[] orbiting = new boolean[1];
        GLInputHandler input = new GLInputHandler() {
            @Override
            public void key(int key, int action, int modifiers) {
                if (action != GLFW_PRESS) return;
                if (key == GLFW_KEY_P) controller.pause();
                if (key == GLFW_KEY_S) controller.step();
                if (key == GLFW_KEY_H || key == GLFW_KEY_HOME) renderer.camera().reset();
            }

            @Override
            public void cursorMoved(double x, double y) {
                if (orbiting[0]) {
                    renderer.camera().orbit((float) (x - previousCursor[0]) * 0.01f,
                            (float) (y - previousCursor[1]) * 0.01f);
                }
                previousCursor[0] = x;
                previousCursor[1] = y;
                cursor[0] = x;
                cursor[1] = y;
            }

            @Override
            public void mouseButton(int button, int action, int modifiers) {
                if (button == GLFW_MOUSE_BUTTON_MIDDLE) orbiting[0] = action != GLFW_RELEASE;
                if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_RELEASE) {
                    controls.click(cursor[0], cursor[1]);
                }
            }

            @Override
            public void scroll(double xOffset, double yOffset) {
                renderer.camera().zoom((float) -yOffset);
            }
        };
        GLWindow window = new GLWindow("MaDKit GL3D — Bees", 1_280, 720, true,
                () -> {
                    renderer.render(snapshots.latestOrEmpty());
                    overlay.render(1_280, 720);
                }, input, () -> {
                    overlay.close();
                    renderer.close();
                });
        window.start();
        try {
            while (window.isOpen() && !Thread.currentThread().isInterrupted()) {
                controller.update();
                snapshots.publish(controller.model().colonySnapshot());
                window.setTitle("MaDKit GL3D Bees | " + controller.state() + " | tick " + controller.model().tick()
                        + " | bees " + controller.model().beeCount());
                Thread.sleep(16);
            }
        } finally {
            window.close();
        }
    }
}