package madkit.simulation;

import org.testng.annotations.Test;

import madkit.kernel.MadkitUnitTestCase;
import madkit.test.utils.EmptySimuLauncher;

public class SimulationEngineTest extends MadkitUnitTestCase {

	@Test
	public void givenSimulationEngine_whenLaunched_works() {
		SimuLauncher s = new EmptySimuLauncher();
		launchTestedAgent(s);
	}

}
