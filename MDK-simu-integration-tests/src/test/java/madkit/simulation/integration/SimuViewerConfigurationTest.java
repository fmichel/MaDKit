package madkit.simulation.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

import madkit.kernel.MadkitConcurrentTestCase;
import madkit.simu.integration.IntegrationLauncher;

/** Verifies the fallback behavior when a simulation declares no viewers. */
public class SimuViewerConfigurationTest extends MadkitConcurrentTestCase {

	@Test
	public void givenNoViewerConfiguration_whenActivated_thenSimulationStartsWithoutViewers() {
		// Given
		IntegrationLauncher launcher = new IntegrationLauncher();

		// When
		launchAgent(launcher);

		// Then
		assertThat(launcher.getViewers()).isEmpty();
		assertThat(launcher.events()).containsExactly("seed-index", "random-generator", "model", "environment",
				"scheduler", "simulated-agents", "viewers", "simulation-start");
	}
}
