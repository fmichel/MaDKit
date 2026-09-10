package madkit.simulation.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import madkit.kernel.MadkitConcurrentTestCase;
import madkit.simu.integration.IntegrationLauncher;

/**
 * Verifies that the MaDKit kernel seed configuration reaches {@link IntegrationLauncher}.
 *
 * <p>The test instances are created by a TestNG factory because the kernel configuration is
 * read before each test method and must therefore be provided before the launcher is activated.
 * </p>
 */
public class SimuSeedConfigurationTest {

	@Factory
	public Object[] createSeedConfigurationTests() {
		return new Object[] {
				new ConfiguredSeedTest(),
				new MissingSeedTest(),
				new SentinelSeedTest() };
	}

	private static final class ConfiguredSeedTest extends SeedConfigurationCase {

		@Test
		public void givenConfiguredSeed_whenLauncherActivates_thenSeedIndexIsConfigured() {
			// Given
			IntegrationLauncher launcher = new IntegrationLauncher();

			// When
			launchAgent(launcher);

			// Then
			assertThat(launcher.getPrngSeedIndex()).isEqualTo(23);
		}

		@Override
		protected String[] getMadkitTestArgs() {
			return new String[] { "--seed", "23" };
		}
	}

	private static final class MissingSeedTest extends SeedConfigurationCase {

		@Test
		public void givenMissingSeed_whenLauncherActivates_thenSeedIndexDefaultsToZero() {
			// Given
			IntegrationLauncher launcher = new IntegrationLauncher();

			// When
			launchAgent(launcher);

			// Then
			assertThat(launcher.getPrngSeedIndex()).isZero();
		}
	}

	private static final class SentinelSeedTest extends SeedConfigurationCase {

		@Test
		public void givenIntegerMinValueSeed_whenLauncherActivates_thenSeedIndexDefaultsToZero() {
			// Given
			IntegrationLauncher launcher = new IntegrationLauncher();

			// When
			launchAgent(launcher);

			// Then
			assertThat(launcher.getPrngSeedIndex()).isZero();
		}

		@Override
		protected String[] getMadkitTestArgs() {
			return new String[] { "--seed", Integer.toString(Integer.MIN_VALUE) };
		}
	}

	private abstract static class SeedConfigurationCase extends MadkitConcurrentTestCase {
	}
}
