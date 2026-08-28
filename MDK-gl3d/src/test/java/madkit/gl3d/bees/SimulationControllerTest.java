package madkit.gl3d.bees;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.testng.annotations.Test;

public class SimulationControllerTest {
    @Test
    public void pausedControllerStepsExactlyOneTick() {
        // Given
        SimulationController controller = new SimulationController(new BeeColonyModel(1, 2, 1));
        controller.start();
        controller.update();
        controller.pause();
        controller.update();
        long pausedTick = controller.model().tick();

        // When
        controller.step();
        controller.update();

        // Then
        assertThat(controller.state()).isEqualTo(SimulationController.State.PAUSED);
        assertThat(controller.model().tick()).isEqualTo(pausedTick + 1);
    }

    @Test
    public void speedRejectsInvalidValues() {
        // Given
        SimulationController controller = new SimulationController(new BeeColonyModel(1, 1, 1));

        // When / Then
        assertThatIllegalArgumentException().isThrownBy(() -> controller.speed(0));
        assertThatIllegalArgumentException().isThrownBy(() -> controller.speed(Double.NaN));
        assertThatIllegalArgumentException().isThrownBy(() -> controller.speed(101));
    }

    @Test
    public void updatePublishesTimeAndTickDuration() {
        // Given
        SimulationController controller = new SimulationController(new BeeColonyModel(2, 20, 2));
        controller.start();

        // When
        controller.update();

        // Then
        assertThat(controller.simulatedTimeSeconds()).isEqualTo(1.0 / 60.0);
        assertThat(controller.lastTickDurationMicros()).isGreaterThanOrEqualTo(0.0);
        controller.pause();
        controller.update();
        double measuredMicros = controller.lastTickDurationMicros();
        controller.update();
        assertThat(controller.model().tick()).isEqualTo(1);
        assertThat(controller.lastTickDurationMicros()).isEqualTo(measuredMicros);
    }
}