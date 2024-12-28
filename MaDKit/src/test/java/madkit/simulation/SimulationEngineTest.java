package madkit.simulation;

import org.testng.annotations.Test;

import madkit.kernel.JunitMadkit;

public class SimulationEngineTest extends JunitMadkit {

	@Test
	public void givenSimulationEngine_whenLaunched_works() {
		SimulationEngine s = new SimulationEngine();
		launchTestedAgent(s);
	}

}
