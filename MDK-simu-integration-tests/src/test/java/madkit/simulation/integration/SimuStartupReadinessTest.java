package madkit.simulation.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

import madkit.kernel.MadkitConcurrentTestCase;
import madkit.simu.integration.IntegrationLauncher;
import madkit.simu.integration.common.IntegrationEnvironment;
import madkit.simu.integration.common.IntegrationModel;
import madkit.simu.integration.common.IntegrationScheduler;
import madkit.simulation.scheduler.TickBasedScheduler;

/**
 * Consumer-level smoke test for the minimum headless simulation startup contract.
 *
 * <p>
 * This test uses the public simulation API for startup and verifies the complete
 * initialization order, including seed-index and random-generator setup.
 * </p>
 */
public class SimuStartupReadinessTest extends MadkitConcurrentTestCase {

	@Test
	public void givenMinimalLauncher_whenActivated_thenSimulationIsReadyInDocumentedOrder() {
		// Given
		IntegrationLauncher launcher = new IntegrationLauncher();

		// When
		launchAgent(launcher);

		// Then
		Object model = launcher.getModel();
		Object environment = launcher.getEnvironment();
		Object scheduler = launcher.getScheduler();
		assertThat(model).isInstanceOf(IntegrationModel.class);
		assertThat(environment).isInstanceOf(IntegrationEnvironment.class);
		assertThat(scheduler).isInstanceOf(IntegrationScheduler.class)
				.isInstanceOf(TickBasedScheduler.class);
		assertThat(launcher.getViewers()).isEmpty();
		assertThat(launcher.prng()).isNotNull();
		assertThat(launcher.events()).containsExactly("seed-index", "random-generator", "model", "environment", "scheduler",
				"simulated-agents", "viewers", "simulation-start");
	}
}
