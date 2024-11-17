package madkit.simulation;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import madkit.kernel.JunitMadkit;
import madkit.simulation.scheduler.TickBasedScheduler;

public class SimulationEngineTest  extends JunitMadkit {

	
	@Test
	public void givenSimulationEngine_whenLaunched_works(){
    	SimulationEngine s = new SimulationEngine();
		launchTestedAgent(s);
	}

}
