package madkit.gl3d.bees;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class GL3dBeeModelTest {
    @Test
    public void resetPublishesReplacementSnapshot() {
        // Given
        GL3dBeeModel model = new GL3dBeeModel();
        model.controller().reset(new BeeColonyModel(99, 3, 2));

        // When
        model.advanceSimulation();
        ColonySnapshot snapshot = model.latestSnapshot();

        // Then
        assertThat(snapshot.tick()).isZero();
        assertThat(snapshot.bees()).hasSize(3);
        assertThat(snapshot.flowers()).hasSize(2);
    }

    @Test
    public void scheduledAdvanceHonorsControllerLifecycle() {
        // Given
        GL3dBeeModel model = new GL3dBeeModel();
        long initialTick = model.latestSnapshot().tick();

        // When
        model.advanceSimulation();

        // Then
        assertThat(model.latestSnapshot().tick()).isEqualTo(initialTick);

        // Given
        model.controller().start();

        // When
        model.advanceSimulation();

        // Then
        assertThat(model.latestSnapshot().tick()).isEqualTo(initialTick + 1);
    }
}
