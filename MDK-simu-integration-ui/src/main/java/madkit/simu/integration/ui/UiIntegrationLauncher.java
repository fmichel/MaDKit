package madkit.simu.integration.ui;

import madkit.simulation.EngineAgents;
import madkit.simulation.SimuLauncher;
import madkit.simu.integration.common.IntegrationAgent;
import madkit.simu.integration.common.IntegrationModel;
import madkit.simu.integration.common.IntegrationScheduler;

/** JavaFX-enabled launcher using the shared integration simulation components. */
@EngineAgents(scheduler = IntegrationScheduler.class, environment = IntegrationUiEnvironment.class,
		model = IntegrationModel.class, viewers = { IntegrationViewer.class })
public class UiIntegrationLauncher extends SimuLauncher {

	@Override
	protected void onLaunchSimulatedAgents() {
		launchAgent(new IntegrationAgent());
	}

	/** Launches the JavaFX integration application. */
	public static void main(String[] args) {
		executeThisAgent();
	}
}
