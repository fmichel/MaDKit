package madkit.kernel;

import static madkit.kernel.Agent.ReturnCode.AGENT_CRASH;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import madkit.test.agents.BugInActivateAgent;
import madkit.test.agents.ThreadedTestAgent;

/**
 * @author Fabien Michel
 *
 */
public class AgentTest extends JunitMadkit{
	
	@Test
	void givenAgentClassWithoutLiveMethod_whenIsThreaded_thenFalseIsReturned() {
		Agent a = new Agent();
		assertFalse(a.isThreaded());
		a = new Agent() {
			@Override
			protected void onActivation() { /* testing inheritance */ }
		};
		assertFalse(a.isThreaded());
	}

	@Test
	void givenAgentClassWithLiveMethod_whenIsThreaded_thenTrueIsReturned() {
		Agent a = new Agent() {
			@Override
			protected void onLiving() {
				super.onLiving();
			}
		};
		assertTrue(a.isThreaded());
	}

	@Test(expectedExceptions = NullPointerException.class)
	void givenAgentNotLaunched_whenNoKernel_thenThrowsNullPointerException(){
		Agent a = new Agent();
		a.getKernelAddress();
	}
	
	@Test
	public void returnSuccess() {
		launchTestedAgent(new BugInActivateAgent() {
		},AGENT_CRASH);
	}

}
