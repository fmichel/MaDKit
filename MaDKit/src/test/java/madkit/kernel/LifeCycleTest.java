package madkit.kernel;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import madkit.kernel.Agent.ReturnCode;
import madkit.test.agents.BugInActivateAgent;
import madkit.test.agents.BugInEndThreadedAgent;
import madkit.test.agents.BugInLiveAgent;
import madkit.test.agents.BugInLiveAndEndAgent;
import madkit.test.agents.ThreadedTestAgent;

/**
 *
 *
 */
public class LifeCycleTest extends MadkitUnitTestCase {
	@Test
	public void returnCrashOnBugInActivate() {
		GenericTestAgent a;
		launchTestedAgent(a = new BugInActivateAgent(), ReturnCode.AGENT_CRASH);
		checkTermination(a);
		assertFalse(a.didPassThroughEnd());
	}

	@Test
	public void terminateOnBugInLive() {
		GenericTestAgent a;
		launchTestedAgent(a = new BugInLiveAgent(), ReturnCode.SUCCESS);
		awaitTermination(a, 1000);
		assertTrue(a.didPassThroughEnd());
	}

	@Test
	public void terminateOnBugInLiveAndEnd() {
		GenericTestAgent a;
		launchTestedAgent(a = new BugInLiveAndEndAgent(), ReturnCode.SUCCESS);
		awaitTermination(a, 1000);
		assertTrue(a.didPassThroughEnd());
	}

	@Test
	public void terminateOnThreadedBugInEnd() {
		GenericTestAgent a;
		launchTestedAgent(a = new BugInEndThreadedAgent(), ReturnCode.SUCCESS);
		awaitTermination(a, 1000);
		assertTrue(a.didPassThroughEnd());
	}

	@Test
	public void nominalThreadedAgent() {
		GenericTestAgent a;
		launchTestedAgent(a = new ThreadedTestAgent(), ReturnCode.SUCCESS);
		awaitTermination(a, 1000);
		assertTrue(a.didPassThroughEnd());
	}

	@Test
	public void nominalAgent() {
		GenericTestAgent a;
		launchTestedAgent(a = new GenericTestAgent(), ReturnCode.SUCCESS);
		assertFalse(a.didPassThroughEnd());
		assertTrue(a.alive.get());
	}

}
