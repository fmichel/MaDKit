package madkit.simulation;

import org.testng.annotations.Test;

import madkit.kernel.EmptySimuLauncher;
import madkit.kernel.JunitMadkit;

public class SimulationEngineTest extends JunitMadkit {

	@Test
	public void givenSimulationEngine_whenLaunched_works() {
		SimuLauncher s = new EmptySimuLauncher();
		launchTestedAgent(s);
	}

}
