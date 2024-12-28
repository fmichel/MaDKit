package madkit.kernel;

import static madkit.kernel.Agent.ReturnCode.AGENT_CRASH;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import madkit.test.agents.BugInActivateAgent;
import madkit.test.agents.EmptyAgent;

/**
 * @author Fabien Michel
 *
 */
public class AgentTest extends JunitMadkit {

	@Test
	void givenAgentClassWithoutLiveMethod_whenIsThreaded_thenFalseIsReturned() {
		Agent a = new EmptyAgent();
		assertFalse(a.isThreaded());
		a = new Agent() {
			@Override
			protected void onActivation() {
				/* testing inheritance */ }
		};
		assertFalse(a.isThreaded());
	}

	@Test
	void givenAgentClassWithLiveMethod_whenIsThreaded_thenTrueIsReturned() {
		Agent a = new Agent() {
			@Override
			protected void onLive() {
				super.onLive();
			}
		};
		assertTrue(a.isThreaded());
	}

	@Test(expectedExceptions = NullPointerException.class)
	void givenAgentNotLaunched_whenNoKernel_thenThrowsNullPointerException() {
		Agent a = new EmptyAgent();
		a.getKernelAddress();
	}

	@Test
	void givenLaunchedAgentLaunched_whenLaunchAgentAgent_thenThrowsIllegalArgumentException() {
		testBehavior(agent -> {
			Agent a = new EmptyAgent();
			launchTestedAgent(a);
			try {
				launchTestedAgent(a);
				fail();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		});
	}

	@Test
	public void returnSuccess() {
		launchTestedAgent(new BugInActivateAgent() {
		}, AGENT_CRASH);
	}

}
