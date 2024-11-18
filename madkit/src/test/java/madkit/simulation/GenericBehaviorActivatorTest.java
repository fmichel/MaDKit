
package madkit.simulation;

import org.testng.annotations.Test;

import madkit.kernel.JunitMadkit;
import madkit.kernel.Agent.ReturnCode;
import madkit.simulation.activator.MethodActivator;
import madkit.simulation.scheduler.TickBasedScheduler;
import madkit.test.agents.CGRAgent;


/**
 * <code>null</code> in the constructor is 
 * covered by {@link BasicSchedulerTest#addingNullActivator()}
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.15
 * @version 0.9
 * 
 */

public class GenericBehaviorActivatorTest extends JunitMadkit {

	private MethodActivator buggy;

	@Test
	public void noSuchMethodCrash() {
		launchTestedAgent(new TickBasedScheduler() {
			@Override
			protected void onActivation() {
				launchAgent(new CGRAgent());
				buggy = new MethodActivator(GROUP, ROLE, "doIt");
				addActivator(buggy);
				try {
					buggy.execute();
					noExceptionFailure();
				} catch (SimulationException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);
	}

	@Test
	public void invocationException() {
		launchTestedAgent(new TickBasedScheduler() {
			@Override
			protected void onActivation() {
				launchAgent(new CGRAgent());
				buggy = new MethodActivator(GROUP, ROLE, "doIt");
				addActivator(buggy);
				try {
					buggy.execute();
					noExceptionFailure();
				} catch (SimulationException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);
	}

}
