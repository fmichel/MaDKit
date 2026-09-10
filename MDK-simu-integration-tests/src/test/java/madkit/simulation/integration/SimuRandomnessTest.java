package madkit.simulation.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.testng.annotations.Test;

import madkit.kernel.MadkitConcurrentTestCase;
import madkit.simu.integration.IntegrationLauncher;

/**
 * Verifies that simulation random values are reproducible from the configured seed index.
 */
public class SimuRandomnessTest extends MadkitConcurrentTestCase {

	@Test
	public void givenSameSeedIndex_whenTwoLaunchersGenerateValues_thenValuesAreEqual() {
		// Given
		SeededIntegrationLauncher firstLauncher = new SeededIntegrationLauncher(17);
		SeededIntegrationLauncher secondLauncher = new SeededIntegrationLauncher(17);
		launchAgent(firstLauncher);
		launchAgent(secondLauncher);

		// When
		List<Long> firstValues = firstLauncher.randomValues();
		List<Long> secondValues = secondLauncher.randomValues();

		// Then
		assertThat(firstValues).containsExactlyElementsOf(secondValues);
	}

	@Test
	public void givenDifferentSeedIndexes_whenTwoLaunchersGenerateValues_thenValuesDiffer() {
		// Given
		SeededIntegrationLauncher firstLauncher = new SeededIntegrationLauncher(17);
		SeededIntegrationLauncher secondLauncher = new SeededIntegrationLauncher(18);
		launchAgent(firstLauncher);
		launchAgent(secondLauncher);

		// When
		List<Long> firstValues = firstLauncher.randomValues();
		List<Long> secondValues = secondLauncher.randomValues();

		// Then
		assertThat(firstValues).isNotEqualTo(secondValues);
	}

	@Test
	public void givenNoConfiguredSeed_whenLauncherActivates_thenDefaultSeedIndexIsZero() {
		// Given
		IntegrationLauncher launcher = new IntegrationLauncher();

		// When
		launchAgent(launcher);

		// Then
		assertThat(launcher.getPrngSeedIndex()).isZero();
	}

	private static final class SeededIntegrationLauncher extends IntegrationLauncher {

		private final int seedIndex;

		private SeededIntegrationLauncher(int seedIndex) {
			this.seedIndex = seedIndex;
		}

		@Override
		protected void onInitializeSimulationSeedIndex() {
			setPrngSeedIndex(seedIndex);
		}

		private List<Long> randomValues() {
			return List.of(prng().nextLong(), prng().nextLong(), prng().nextLong(), prng().nextLong());
		}
	}
}
