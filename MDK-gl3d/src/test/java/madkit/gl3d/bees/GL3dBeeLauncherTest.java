package madkit.gl3d.bees;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

import madkit.simulation.EngineAgents;

public class GL3dBeeLauncherTest {
    @Test
    public void launcherDeclaresAllRequiredSimulationAgents() {
        // Given
        EngineAgents agents = GL3dBeeLauncher.class.getAnnotation(EngineAgents.class);

        // When
        boolean hasViewer = agents != null && agents.viewers().length == 1
                && agents.viewers()[0] == GL3dBeeViewer.class;

        // Then
        assertThat(agents).isNotNull();
        assertThat(agents.model()).isEqualTo(GL3dBeeModel.class);
        assertThat(agents.environment()).isEqualTo(GL3dBeeEnvironment.class);
        assertThat(agents.scheduler()).isEqualTo(GL3dBeeScheduler.class);
        assertThat(hasViewer).isTrue();
    }
}
