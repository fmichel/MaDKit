
package madkit.simulation;

import org.testng.annotations.Test;

import madkit.kernel.Agent.ReturnCode;
import madkit.kernel.MadkitUnitTestCase;
import madkit.simulation.scheduler.MethodActivator;
import madkit.simulation.scheduler.TickBasedScheduler;
import madkit.test.agents.CGRAgent;

/**
 * 
 *
 * @since MaDKit 5.0.0.15
 * @version 0.9
 * 
 */

public class GenericBehaviorActivatorTest extends MadkitUnitTestCase {

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
				} catch (SimuException e) {
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
				} catch (SimuException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);
	}

}
