package madkit.simulation.integration.ui;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

import madkit.simulation.EngineAgents;
import madkit.simu.integration.ui.IntegrationUiEnvironment;
import madkit.simu.integration.ui.IntegrationViewer;
import madkit.simu.integration.ui.UiIntegrationLauncher;

/** Verifies the UI application declares its independent engine composition. */
public class UiIntegrationLauncherTest {

	@Test
	public void givenUiLauncher_whenReadingEngineConfiguration_thenViewerIsDeclared() {
		// Given
		Class<UiIntegrationLauncher> launcherType = UiIntegrationLauncher.class;

		// When
		EngineAgents configuration = launcherType.getAnnotation(EngineAgents.class);

		// Then
		assertThat(configuration).isNotNull();
		assertThat(configuration.environment()).isEqualTo(IntegrationUiEnvironment.class);
		assertThat(configuration.model()).isEqualTo(madkit.simu.integration.common.IntegrationModel.class);
		assertThat(configuration.scheduler()).isEqualTo(madkit.simu.integration.common.IntegrationScheduler.class);
		assertThat(configuration.viewers()).containsExactly(IntegrationViewer.class);
	}
}
