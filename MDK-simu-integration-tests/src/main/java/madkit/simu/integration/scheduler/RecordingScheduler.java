package madkit.simu.integration.scheduler;

import madkit.simu.integration.common.IntegrationScheduler;

public final class RecordingScheduler extends IntegrationScheduler {

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
