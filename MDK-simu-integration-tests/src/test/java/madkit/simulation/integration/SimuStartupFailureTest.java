package madkit.simulation.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

import madkit.kernel.Agent.ReturnCode;
import madkit.kernel.MadkitConcurrentTestCase;
import madkit.kernel.Scheduler;
import madkit.simu.integration.IntegrationLauncher;
import madkit.simu.integration.common.IntegrationEnvironment;
import madkit.simu.integration.common.IntegrationModel;
import madkit.simu.integration.common.IntegrationScheduler;
import madkit.simulation.EngineAgents;
import madkit.simulation.SimuAgent;
import madkit.simulation.SimuEnvironment;
import madkit.simulation.SimuModel;
import madkit.simulation.Viewer;

/**
 * Verifies that failures during engine-agent startup stop the simulation startup
 * lifecycle.
 */
public class SimuStartupFailureTest extends MadkitConcurrentTestCase {

	@Test
	public void givenModelLaunchFailure_whenActivated_thenStartupStopsBeforeModelIsRecorded() {
		// Given
		FailingModelLauncher launcher = new FailingModelLauncher();

		// When
		launchAgent(launcher);

		// Then
		assertThat(launcher.isAlive()).isFalse();
		assertThat(launcher.events()).containsExactly("seed-index", "random-generator");
	}

	@Test
	public void givenEnvironmentLaunchFailure_whenActivated_thenStartupStopsAfterModelLaunch() {
		// Given
		FailingEnvironmentLauncher launcher = new FailingEnvironmentLauncher();

		// When
		launchAgent(launcher);

		// Then
		assertThat(launcher.isAlive()).isFalse();
		assertThat(launcher.events()).containsExactly("seed-index", "random-generator", "model");
	}

	@Test
	public void givenSchedulerLaunchFailure_whenActivated_thenStartupStopsAfterEnvironmentLaunch() {
		// Given
		FailingSchedulerLauncher launcher = new FailingSchedulerLauncher();

		// When
		launchAgent(launcher);

		// Then
		assertThat(launcher.isAlive()).isFalse();
		assertThat(launcher.events()).containsExactly("seed-index", "random-generator", "model", "environment");
	}

	@Test
	public void givenSimulatedAgentLaunchFailure_whenActivated_thenStartupStopsBeforeSimulationStart() {
		// Given
		FailingSimulatedAgentLauncher launcher = new FailingSimulatedAgentLauncher();

		// When
		launchAgent(launcher);

		// Then
		assertThat(launcher.isAlive()).isFalse();
		assertThat(launcher.events()).containsExactly("seed-index", "random-generator", "model", "environment",
				"scheduler");
	}

	@Test
	public void givenViewerLaunchFailure_whenActivated_thenStartupStopsBeforeSimulationStart() {
		// Given
		FailingViewerLauncher launcher = new FailingViewerLauncher();

		// When
		launchAgent(launcher);

		// Then
		assertThat(launcher.isAlive()).isFalse();
		assertThat(launcher.events()).containsExactly("seed-index", "random-generator", "model", "environment",
				"scheduler", "simulated-agents");
	}

	@Test
	public void givenSimulationStartFailure_whenActivated_thenStartupDoesNotComplete() {
		// Given
		FailingSimulationStartLauncher launcher = new FailingSimulationStartLauncher();

		// When
		launchAgent(launcher);

		// Then
		assertThat(launcher.isAlive()).isFalse();
		assertThat(launcher.events()).containsExactly("seed-index", "random-generator", "model", "environment",
				"scheduler", "simulated-agents", "viewers");
	}

	@EngineAgents(scheduler = IntegrationScheduler.class, environment = IntegrationEnvironment.class, model = IntegrationModel.class)
	private static final class FailingModelLauncher extends IntegrationLauncher {

		@Override
		protected <M extends SimuModel> M onLaunchModel() {
			throw new IllegalStateException("model startup failure");
		}
	}

	@EngineAgents(scheduler = IntegrationScheduler.class, environment = IntegrationEnvironment.class, model = IntegrationModel.class)
	private static final class FailingEnvironmentLauncher extends IntegrationLauncher {

		@Override
		protected <E extends SimuEnvironment> E onLaunchEnvironment() {
			throw new IllegalStateException("environment startup failure");
		}
	}

	@EngineAgents(scheduler = IntegrationScheduler.class, environment = IntegrationEnvironment.class, model = IntegrationModel.class)
	private static final class FailingSchedulerLauncher extends IntegrationLauncher {

		@Override
		protected <S extends Scheduler<?>> S onLaunchScheduler() {
			throw new IllegalStateException("scheduler startup failure");
		}
	}

	@EngineAgents(scheduler = IntegrationScheduler.class, environment = IntegrationEnvironment.class, model = IntegrationModel.class)
	public static final class FailingSimulatedAgentLauncher extends IntegrationLauncher {

		public FailingSimulatedAgentLauncher() {
		}

		@Override
		protected void onLaunchSimulatedAgents() {
			if (launchAgent(new FailingSimulatedAgent()) != ReturnCode.AGENT_CRASH) {
				throw new IllegalStateException("simulated-agent startup failure was not reported");
			}
			throw new IllegalStateException("simulated-agent startup failure");
		}
	}

	@EngineAgents(scheduler = IntegrationScheduler.class, environment = IntegrationEnvironment.class, model = IntegrationModel.class,
			viewers = FailingViewer.class)
	public static final class FailingViewerLauncher extends IntegrationLauncher {

		public FailingViewerLauncher() {
		}

		@Override
		protected void onLaunchViewers() {
			launchAgent(new FailingViewer());
		}
	}

	@EngineAgents(scheduler = IntegrationScheduler.class, environment = IntegrationEnvironment.class, model = FailingSimulationStartModel.class)
	public static final class FailingSimulationStartLauncher extends IntegrationLauncher {

		public FailingSimulationStartLauncher() {
		}
	}

	public static final class FailingSimulatedAgent extends SimuAgent {

		public FailingSimulatedAgent() {
		}

		@Override
		protected void onActivation() {
			throw new IllegalStateException("simulated-agent startup failure");
		}
	}

	public static final class FailingViewer extends Viewer {

		public FailingViewer() {
			throw new IllegalStateException("viewer construction failure");
		}

		@Override
		public void render() {
			// Rendering is never reached when activation fails.
		}
	}

	public static final class FailingSimulationStartModel extends IntegrationModel {

		public FailingSimulationStartModel() {
		}

		@Override
		public void onSimulationStart() {
			super.onSimulationStart();
			throw new IllegalStateException("simulation-start failure");
		}
	}
}
