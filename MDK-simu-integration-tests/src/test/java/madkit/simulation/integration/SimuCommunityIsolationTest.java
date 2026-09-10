package madkit.simulation.integration;


import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import madkit.kernel.MadkitConcurrentTestCase;
import madkit.simu.integration.IntegrationLauncher;

/** Verifies that multiple simulation launchers remain isolated within one kernel. */
public class SimuCommunityIsolationTest extends MadkitConcurrentTestCase {

	@Test
	public void givenTwoLaunchers_whenActivatedInSameKernel_thenTheyUseDistinctCommunities() {
		// Given
		IntegrationLauncher firstLauncher = new IntegrationLauncher();
		IntegrationLauncher secondLauncher = new IntegrationLauncher();

		// When
		launchAgent(firstLauncher);
		launchAgent(secondLauncher);

		// Then
		Assertions.assertThat(firstLauncher.getCommunity()).isNotEqualTo(secondLauncher.getCommunity());
		Assertions.assertThat((Object) firstLauncher.getModel()).isNotSameAs(secondLauncher.getModel());
		Assertions.assertThat((Object) firstLauncher.getEnvironment()).isNotSameAs(secondLauncher.getEnvironment());
		Assertions.assertThat((Object) firstLauncher.getScheduler()).isNotSameAs(secondLauncher.getScheduler());
		Assertions.assertThat((Object) firstLauncher.prng()).isNotSameAs(secondLauncher.prng());
	}
}
