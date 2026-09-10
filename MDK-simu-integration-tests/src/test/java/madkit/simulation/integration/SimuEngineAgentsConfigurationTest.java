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

/** Verifies that simulation engine agents can be configured through annotations. */
public class SimuEngineAgentsConfigurationTest extends MadkitConcurrentTestCase {

	@Test
	public void givenAnnotatedLauncherWithViewers_whenActivated_thenBothDeclaredViewersAreLaunched() {
		// Given
		AnnotatedViewerLauncher launcher = new AnnotatedViewerLauncher();

		// When
		launchAgent(launcher);

		// Then
		assertThat(launcher.getViewers()).hasSize(2);
		assertThat(launcher.getViewers().get(0)).isExactlyInstanceOf(FirstViewer.class);
		assertThat(launcher.getViewers().get(1)).isExactlyInstanceOf(SecondViewer.class);
	}

	@Test
	public void givenAnnotatedLauncher_whenActivated_thenDeclaredModelIsLaunched() {
		// Given
		AnnotatedEngineLauncher launcher = new AnnotatedEngineLauncher();

		// When
		launchAgent(launcher);

		// Then
		RecordingModel model = launcher.getModel();
		assertThat(model).isExactlyInstanceOf(RecordingModel.class);
		assertThat(model.getSimulationStarts()).isEqualTo(1);
	}

	@Test
	public void givenAnnotatedLauncher_whenActivated_thenDeclaredEnvironmentIsLaunched() {
		// Given
		AnnotatedEngineLauncher launcher = new AnnotatedEngineLauncher();

		// When
		launchAgent(launcher);

		// Then
		RecordingEnvironment environment = launcher.getEnvironment();
		assertThat(environment).isExactlyInstanceOf(RecordingEnvironment.class);
		assertThat(environment.getSimulationStarts()).isEqualTo(1);
	}

	@Test
	public void givenAnnotatedLauncher_whenActivated_thenDeclaredSchedulerIsLaunched() {
		// Given
		AnnotatedEngineLauncher launcher = new AnnotatedEngineLauncher();

		// When
		launchAgent(launcher);

		// Then
		RecordingScheduler scheduler = launcher.getScheduler();
		assertThat(scheduler).isExactlyInstanceOf(RecordingScheduler.class);
		assertThat(scheduler.isSimulationStarted()).isTrue();
	}

	@EngineAgents(scheduler = IntegrationScheduler.class, environment = IntegrationEnvironment.class, model = IntegrationModel.class, viewers = {
			FirstViewer.class, SecondViewer.class })
	public static final class AnnotatedViewerLauncher extends IntegrationLauncher {

		public AnnotatedViewerLauncher() {
		}
	}

	@EngineAgents(scheduler = RecordingScheduler.class, environment = RecordingEnvironment.class, model = RecordingModel.class)
	public static final class AnnotatedEngineLauncher extends IntegrationLauncher {

		public AnnotatedEngineLauncher() {
		}
	}

	public static final class RecordingModel extends IntegrationModel {

		public RecordingModel() {
		}
	}

	public static final class RecordingEnvironment extends IntegrationEnvironment {

		public RecordingEnvironment() {
		}
	}

	public static final class RecordingScheduler extends IntegrationScheduler {

		private boolean simulationStarted;

		public RecordingScheduler() {
		}

		@Override
		public void onSimulationStart() {
			super.onSimulationStart();
			simulationStarted = true;
		}

		public boolean isSimulationStarted() {
			return simulationStarted;
		}
	}

	public static final class FirstViewer extends Viewer {

		public FirstViewer() {
		}

		@Override
		public void render() {
			// No rendering is needed for this engine-wiring test.
		}
	}

	public static final class SecondViewer extends Viewer {

		public SecondViewer() {
		}

		@Override
		public void render() {
			// No rendering is needed for this engine-wiring test.
		}
	}
}