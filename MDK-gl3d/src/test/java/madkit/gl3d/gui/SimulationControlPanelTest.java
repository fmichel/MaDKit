package madkit.gl3d.gui;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import org.testng.annotations.Test;

import madkit.gl3d.bees.BeeColonyModel;
import madkit.gl3d.bees.SimulationController;

public class SimulationControlPanelTest {
    @Test
    public void pauseButtonQueuesPauseOnlyWhenRunning() {
        // Given
        SimulationController controller = new SimulationController(new BeeColonyModel(1, 2, 1));
        AtomicReference<String> action = new AtomicReference<>();
        SimulationControlPanel panel = new SimulationControlPanel(controller, action::set);
        SimulationControlPanel.Button pause = panel.buttons().get(1);

        // When
        assertThat(panel.click(pause.x() + 1, pause.y() + 1)).isFalse();
        controller.start();
        controller.update();
        assertThat(panel.click(pause.x() + 1, pause.y() + 1)).isTrue();
        controller.update();

        // Then
        assertThat(action).hasValue("pause");
        assertThat(controller.state()).isEqualTo(SimulationController.State.PAUSED);
    }

    @Test
    public void stepButtonIsEnabledOnlyWhilePaused() {
        // Given
        SimulationController controller = new SimulationController(new BeeColonyModel(1, 2, 1));
        SimulationControlPanel panel = new SimulationControlPanel(controller, ignored -> { });
        SimulationControlPanel.Button step = panel.buttons().get(2);

        // When / Then
        assertThat(panel.isEnabled(step)).isFalse();
        controller.start();
        controller.update();
        controller.pause();
        controller.update();
        assertThat(panel.isEnabled(step)).isTrue();
    }

    @Test
    public void statusAndLabelsExposeCurrentSimulationState() {
        // Given
        SimulationController controller = new SimulationController(new BeeColonyModel(7, 3, 2));
        SimulationControlPanel panel = new SimulationControlPanel(controller, ignored -> { });

        // When
        SimulationStatus status = panel.status(59.5);

        // Then
        assertThat(panel.buttons()).extracting(SimulationControlPanel.Button::label)
                .containsExactly("Start", "Pause", "Step", "Stop", "Reset", "Slower", "Faster",
                        "Bees-", "Bees+", "Flowers-", "Flowers+", "Seed+", "Colors", "Vectors", "Trails", "Labels");
        assertThat(status.state()).isEqualTo(SimulationController.State.STOPPED);
        assertThat(status.tick()).isZero();
        assertThat(status.beeCount()).isEqualTo(3);
        assertThat(status.renderFps()).isEqualTo(59.5);
        assertThat(status.renderFrameMicros()).isZero();
        assertThat(status.simulatedTimeSeconds()).isZero();
        assertThat(status.simulationTickMicros()).isZero();
        assertThat(status.stateCounts()).containsEntry(madkit.gl3d.bees.BeeState.IN_HIVE, 3);
        assertThat(status.stateCounts()).hasSize(madkit.gl3d.bees.BeeState.values().length);
    }

    @Test
    public void startButtonResumesPausedSimulation() {
        // Given
        SimulationController controller = new SimulationController(new BeeColonyModel(3, 2, 1));
        AtomicReference<String> action = new AtomicReference<>();
        SimulationControlPanel panel = new SimulationControlPanel(controller, action::set);
        controller.start();
        controller.update();
        controller.pause();
        controller.update();
        SimulationControlPanel.Button start = panel.buttons().get(0);

        // When
        assertThat(panel.click(start.x() + 1, start.y() + 1)).isTrue();
        controller.update();

        // Then
        assertThat(action).hasValue("start");
        assertThat(controller.state()).isEqualTo(SimulationController.State.RUNNING);
    }

    @Test
    public void speedButtonsQueueBoundedSpeedChanges() {
        // Given
        SimulationController controller = new SimulationController(new BeeColonyModel(9, 1, 1));
        SimulationControlPanel panel = new SimulationControlPanel(controller, ignored -> { });
        SimulationControlPanel.Button slower = panel.buttons().get(5);
        SimulationControlPanel.Button faster = panel.buttons().get(6);

        // When
        assertThat(panel.click(slower.x() + 1, slower.y() + 1)).isTrue();
        controller.update();
        assertThat(panel.click(faster.x() + 1, faster.y() + 1)).isTrue();
        controller.update();

        // Then
        assertThat(controller.speed()).isEqualTo(1.0);
        controller.speed(100.0);
        controller.update();
        panel.click(faster.x() + 1, faster.y() + 1);
        controller.update();
        assertThat(controller.speed()).isEqualTo(100.0);
    }

    @Test
    public void scenarioButtonsQueueDeterministicReset() {
        // Given
        SimulationController controller = new SimulationController(new BeeColonyModel(42, 20, 3));
        SimulationControlPanel panel = new SimulationControlPanel(controller, ignored -> { });
        SimulationControlPanel.Button beesUp = panel.buttons().get(8);
        SimulationControlPanel.Button flowersDown = panel.buttons().get(9);

        // When
        assertThat(panel.click(beesUp.x() + 1, beesUp.y() + 1)).isTrue();
        assertThat(panel.click(flowersDown.x() + 1, flowersDown.y() + 1)).isTrue();
        controller.update();

        // Then
        assertThat(panel.scenarioParameters().beeCount()).isEqualTo(30);
        assertThat(panel.scenarioParameters().flowerCount()).isEqualTo(2);
        assertThat(controller.model().beeCount()).isEqualTo(30);
        assertThat(controller.model().flowerCount()).isEqualTo(2);
        assertThat(controller.state()).isEqualTo(SimulationController.State.STOPPED);
    }

    @Test
    public void colorsButtonTogglesRenderOptionOnly() {
        // Given
        SimulationController controller = new SimulationController(new BeeColonyModel(4, 2, 1));
        SimulationControlPanel panel = new SimulationControlPanel(controller, ignored -> { });
        SimulationControlPanel.Button colors = panel.buttons().get(12);

        // When
        assertThat(panel.stateColors()).isTrue();
        assertThat(panel.click(colors.x() + 1, colors.y() + 1)).isTrue();

        // Then
        assertThat(panel.stateColors()).isFalse();
        assertThat(controller.model().tick()).isZero();
    }

    @Test
    public void vectorsButtonTogglesRenderOptionOnly() {
        // Given
        SimulationController controller = new SimulationController(new BeeColonyModel(4, 2, 1));
        SimulationControlPanel panel = new SimulationControlPanel(controller, ignored -> { });
        SimulationControlPanel.Button vectors = panel.buttons().get(13);

        // When
        assertThat(panel.debugVectors()).isFalse();
        assertThat(panel.click(vectors.x() + 1, vectors.y() + 1)).isTrue();

        // Then
        assertThat(panel.debugVectors()).isTrue();
        assertThat(controller.model().tick()).isZero();
    }

    @Test
    public void trailsButtonTogglesRenderOptionOnly() {
        // Given
        SimulationController controller = new SimulationController(new BeeColonyModel(4, 2, 1));
        SimulationControlPanel panel = new SimulationControlPanel(controller, ignored -> { });
        SimulationControlPanel.Button trails = panel.buttons().get(14);

        // When
        assertThat(panel.trails()).isFalse();
        assertThat(panel.click(trails.x() + 1, trails.y() + 1)).isTrue();

        // Then
        assertThat(panel.trails()).isTrue();
        assertThat(controller.model().tick()).isZero();
    }

    @Test
    public void labelsButtonTogglesRenderOptionOnly() {
        // Given
        SimulationController controller = new SimulationController(new BeeColonyModel(4, 2, 1));
        SimulationControlPanel panel = new SimulationControlPanel(controller, ignored -> { });
        SimulationControlPanel.Button labels = panel.buttons().get(15);

        // When
        assertThat(panel.labels()).isFalse();
        assertThat(panel.click(labels.x() + 1, labels.y() + 1)).isTrue();

        // Then
        assertThat(panel.labels()).isTrue();
        assertThat(controller.model().tick()).isZero();
    }

    @Test
    public void statusPublishesMeasuredRenderFrameTime() {
        // Given
        SimulationController controller = new SimulationController(new BeeColonyModel(8, 1, 1));
        SimulationControlPanel panel = new SimulationControlPanel(controller, ignored -> { });

        // When
        SimulationStatus status = panel.status(60.0, 16_666.7);

        // Then
        assertThat(status.renderFps()).isEqualTo(60.0);
        assertThat(status.renderFrameMicros()).isEqualTo(16_666.7);
        assertThat(controller.model().tick()).isZero();
    }
}