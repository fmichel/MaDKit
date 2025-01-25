package madkit.simulation;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

import madkit.kernel.MadkitUnitTestCase;
import madkit.simulation.scheduler.TickBasedScheduler;

public class SimuLauncherTest extends MadkitUnitTestCase {

	@Test
	public void givenSimuLauncher_whenInitialized_thenDefaultValuesAreCorrect() {
		// Given: a new SimuLauncher instance
		SimuLauncher simuLauncher = new SimuLauncher() {
			protected void onLaunchSimulatedAgents() {
			};
		};
		// When: the SimuLauncher is initialized
		launchAgent(simuLauncher);
		// Then: the default values should be correct
		assertThat(simuLauncher.getCommunity()).isEqualTo(simuLauncher.getClass().getSimpleName());
		assertThat(simuLauncher.getModelGroup()).isEqualTo(SimuOrganization.MODEL_GROUP);
		assertThat(simuLauncher.getEngineGroup()).isEqualTo(SimuOrganization.ENGINE_GROUP);
		assertThat(simuLauncher.getPRNGSeedIndex()).isEqualTo(0);
		assertThat(simuLauncher.getScheduler().getClass()).isEqualTo(TickBasedScheduler.class);
		assertThat(simuLauncher.prng()).isNotNull();
		assertThat(simuLauncher.getViewers()).isEmpty();
		;
	};

}
