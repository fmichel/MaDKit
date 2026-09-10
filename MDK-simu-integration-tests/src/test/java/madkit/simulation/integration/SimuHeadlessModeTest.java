package madkit.simulation.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

import madkit.kernel.MadkitConcurrentTestCase;
import madkit.simu.integration.IntegrationLauncher;
import madkit.simu.integration.common.IntegrationEnvironment;
import madkit.simu.integration.common.IntegrationModel;
import madkit.simu.integration.common.IntegrationScheduler;
import madkit.simulation.EngineAgents;
import madkit.simulation.Viewer;

/** Verifies that headless mode suppresses viewers declared by a simulation launcher. */
public class SimuHeadlessModeTest extends MadkitConcurrentTestCase {

	@Override
	protected String[] getMadkitTestArgs() {
		return new String[] { "--headless" };
	}

	@Test
	public void givenViewerConfiguredAndHeadlessModeEnabled_whenActivated_thenNoViewerIsLaunched() {
		// Given
		HeadlessViewerLauncher launcher = new HeadlessViewerLauncher();
		RecordingViewer.activated = false;

		// When
		launchAgent(launcher);

		// Then
		assertThat(launcher.getViewers()).isEmpty();
		assertThat(RecordingViewer.activated).isFalse();
		assertThat(launcher.events()).contains("viewers", "simulation-start");
	}

	@EngineAgents(scheduler = IntegrationScheduler.class, environment = IntegrationEnvironment.class,
			model = IntegrationModel.class, viewers = RecordingViewer.class)
	public static class HeadlessViewerLauncher extends IntegrationLauncher {

		public HeadlessViewerLauncher() {
		}
	}

	/** Viewer fixture whose activation proves whether headless suppression was bypassed. */
	public static class RecordingViewer extends Viewer {

		private static volatile boolean activated;

		public RecordingViewer() {
		}

		@Override
		protected void onActivation() {
			super.onActivation();
			activated = true;
		}

		@Override
		public void render() {
			// No rendering is needed for this headless-mode test.
		}
	}
}