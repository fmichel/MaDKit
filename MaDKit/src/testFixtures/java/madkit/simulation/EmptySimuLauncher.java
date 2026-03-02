package madkit.simulation;

import madkit.kernel.MadkitConcurrentTestCase;
import madkit.simulation.SimuLauncher;

public class EmptySimuLauncher extends SimuLauncher {

	private MadkitConcurrentTestCase madkitConcurrentTestCase;

	@Override
	protected void onLaunchSimulatedAgents() {

	}

	public MadkitConcurrentTestCase getMadkitConcurrentTestCase() {
		return madkitConcurrentTestCase;
	}

	public void setMadkitConcurrentTestCase(MadkitConcurrentTestCase mdkitConcurrentTestCase) {
		this.madkitConcurrentTestCase = mdkitConcurrentTestCase;
	}

}
