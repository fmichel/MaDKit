package madkit.gl3d.gui;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import madkit.gl3d.bees.BeeColonyModel;
import madkit.gl3d.bees.ScenarioParameters;
import madkit.gl3d.bees.SimulationController;

/**
 * Renderer-independent control surface for the simulation GUI.
 *
 * <p>Native GUI adapters translate pointer coordinates into {@link #click(double, double)}
 * calls. Commands are delegated to the controller and never invoke agents directly.</p>
 */
public final class SimulationControlPanel {
    public record Button(String id, String label, double x, double y, double width, double height) {
        public boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }

    private final SimulationController controller;
    private final List<Button> buttons;
    private final Consumer<String> actionListener;
    private ScenarioParameters parameters;
    private boolean stateColors = true;
    private boolean debugVectors;
    private boolean trails;
    private boolean labels;

    public SimulationControlPanel(SimulationController controller, Consumer<String> actionListener) {
        this.controller = Objects.requireNonNull(controller, "controller");
        this.actionListener = Objects.requireNonNull(actionListener, "actionListener");
        this.parameters = new ScenarioParameters(controller.model().seed(), controller.model().beeCount(),
                controller.model().flowerCount());
        buttons = List.of(
                new Button("start", "Start", 10, 10, 90, 30),
                new Button("pause", "Pause", 105, 10, 90, 30),
                new Button("step", "Step", 200, 10, 90, 30),
                new Button("stop", "Stop", 295, 10, 90, 30),
                new Button("reset", "Reset", 390, 10, 90, 30),
                new Button("speed-down", "Slower", 485, 10, 90, 30),
                new Button("speed-up", "Faster", 580, 10, 90, 30),
                new Button("bees-down", "Bees-", 485, 48, 90, 30),
                new Button("bees-up", "Bees+", 580, 48, 90, 30),
                new Button("flowers-down", "Flowers-", 675, 48, 90, 30),
                new Button("flowers-up", "Flowers+", 770, 48, 90, 30),
                new Button("seed-next", "Seed+", 865, 48, 90, 30),
                new Button("colors", "Colors", 960, 48, 90, 30),
                new Button("vectors", "Vectors", 1055, 48, 90, 30),
                new Button("trails", "Trails", 1150, 48, 90, 30),
                new Button("labels", "Labels", 10, 86, 90, 30));
    }

    public List<Button> buttons() {
        return buttons;
    }

    public boolean isEnabled(Button button) {
        return switch (button.id()) {
            case "start" -> controller.state() == SimulationController.State.STOPPED
                    || controller.state() == SimulationController.State.PAUSED;
            case "pause" -> controller.state() == SimulationController.State.RUNNING;
            case "step" -> controller.state() == SimulationController.State.PAUSED;
            case "stop" -> controller.state() != SimulationController.State.STOPPED;
            case "reset" -> true;
            case "speed-down", "speed-up" -> true;
            case "bees-down", "bees-up", "flowers-down", "flowers-up", "seed-next", "colors", "vectors", "trails", "labels" -> true;
            default -> false;
        };
    }

    /** Routes a click to the enabled button under the provided viewport coordinates. */
    public boolean click(double mouseX, double mouseY) {
        for (Button button : buttons) {
            if (button.contains(mouseX, mouseY) && isEnabled(button)) {
                switch (button.id()) {
                    case "start" -> controller.start();
                    case "pause" -> controller.pause();
                    case "step" -> controller.step();
                    case "stop" -> controller.stop();
                    case "reset" -> controller.reset(new BeeColonyModel(controller.model().seed(), controller.model().beeCount(), controller.model().flowerCount()));
                    case "speed-down" -> controller.speed(Math.max(0.25, controller.speed() * 0.5));
                    case "speed-up" -> controller.speed(Math.min(100.0, controller.speed() * 2.0));
                    case "bees-down" -> changeScenario(parameters.withBeeCount(parameters.beeCount() - ScenarioParameters.COUNT_STEP));
                    case "bees-up" -> changeScenario(parameters.withBeeCount(parameters.beeCount() + ScenarioParameters.COUNT_STEP));
                    case "flowers-down" -> changeScenario(parameters.withFlowerCount(parameters.flowerCount() - 1));
                    case "flowers-up" -> changeScenario(parameters.withFlowerCount(parameters.flowerCount() + 1));
                    case "seed-next" -> changeScenario(parameters.withSeed(parameters.seed() + 1));
                    case "colors" -> stateColors = !stateColors;
                    case "vectors" -> debugVectors = !debugVectors;
                    case "trails" -> trails = !trails;
                    case "labels" -> labels = !labels;
                    default -> throw new IllegalStateException("Unknown button: " + button.id());
                }
                actionListener.accept(button.id());
                return true;
            }
        }
        return false;
    }

    /** Returns an immutable status snapshot including the latest render frame time. */
    public SimulationStatus status(double renderFps) {
        return status(renderFps, 0, 0);
    }

    /** Returns an immutable status snapshot including render-loop and GPU timing diagnostics. */
    public SimulationStatus status(double renderFps, double renderFrameMicros) {
        return status(renderFps, renderFrameMicros, 0);
    }

    /** Returns an immutable status snapshot with wall-clock and GPU frame measurements. */
    public SimulationStatus status(double renderFps, double renderFrameMicros, double gpuFrameMicros) {
        return new SimulationStatus(controller.state(), controller.model().tick(), controller.model().beeCount(),
                controller.model().foodStore(), controller.speed(), renderFps, controller.model().stateCounts(),
                controller.simulatedTimeSeconds(), controller.lastTickDurationMicros(), renderFrameMicros,
                gpuFrameMicros);
    }

    /** Returns the pending scenario configuration shown by the edit controls. */
    public ScenarioParameters scenarioParameters() {
        return parameters;
    }

    /** Returns whether bee state colors are enabled in the renderer. */
    public boolean stateColors() {
        return stateColors;
    }

    /** Returns whether velocity vectors should be rendered for bees. */
    public boolean debugVectors() {
        return debugVectors;
    }

    /** Returns whether bounded bee trails should be rendered. */
    public boolean trails() {
        return trails;
    }

    /** Returns whether labels should be rendered. */
    public boolean labels() {
        return labels;
    }

    private void changeScenario(ScenarioParameters next) {
        parameters = next;
        controller.reset(new BeeColonyModel(next.seed(), next.beeCount(), next.flowerCount()));
    }
}